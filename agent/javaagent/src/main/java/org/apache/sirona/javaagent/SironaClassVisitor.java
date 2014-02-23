/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sirona.javaagent;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SironaClassVisitor extends ClassVisitor implements Opcodes {
    private static final int CONSTANT_ACCESS = ACC_PRIVATE | ACC_STATIC | ACC_FINAL;

    private static final String STATIC_INIT = "<clinit>";
    private static final String CONSTRUCTOR = "<init>";
    private static final String NO_PARAM_RETURN_VOID = "()V";

    private static final String METHOD_SUFFIX = "_$_$irona_$_internal_$_original_$_";
    private static final String FIELD_SUFFIX = "_$_$IRONA_$_INTERNAL_$_KEY";
    private static final String STATIC_CLINT_MERGE_PREFIX = "_$_$irona_static_merge";

    private static final Type KEY_TYPE = Type.getType(String.class);
    private static final Type AGENT_CONTEXT = Type.getType(AgentContext.class);

    private final String javaName;
    private Type classType;

    public SironaClassVisitor(final ClassWriter writer, final String javaName, final Map<String, String> keys) {
        super(ASM4, new SironaStaticInitMerger(writer, keys));
        this.javaName = javaName;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName,
                      final String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        classType = Type.getType("L" + name.replace('.', '/') + ";");
        SironaStaticInitMerger.class.cast(cv).initSironaFields(classType);
    }

    @Override
    public void visitSource(final String source, final String debug) {
        super.visitSource(source, debug);
        visitAnnotation("L" + Instrumented.class.getName().replace('.', '/') + ";", true).visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (!isSironable(access, name)) {
            return visitor;
        }

        final String label = javaName.replace("/", ".") + "." + name;
        if (AgentContext.listeners(label) != null) {
            // generate internal method - the proxy (previous one) delegates to this one
            return new MoveAnnotationOnProxy(
                    new ProxyMethodsVisitor(visitor, access, new Method(name, desc), classType),
                    super.visitMethod(forcePrivate(access), name + METHOD_SUFFIX, desc, signature, exceptions));
        }
        return visitor;
    }

    private static int forcePrivate(final int access) {
        return (access & ~(Modifier.PRIVATE | Modifier.PUBLIC | Modifier.PROTECTED)) | Modifier.PRIVATE;
    }

    private static boolean isSironable(final int access, final String name) {
        return !name.equals(STATIC_INIT) && !name.equals(CONSTRUCTOR) && !Modifier.isAbstract(access) && !Modifier.isNative(access);
    }

    private static class ProxyMethodsVisitor extends GeneratorAdapter {
        private static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
        private static final Type[] STOP_WITH_THROWABLE_ARGS_TYPES = new Type[] {THROWABLE_TYPE};
        private static final Type OBJECT_TYPE = Type.getType(Object.class);
        private static final Type[] STOP_WITH_OBJECT_ARGS_TYPES = new Type[] {OBJECT_TYPE};
        private static final Type[] START_ARGS_TYPES = new Type[] {KEY_TYPE, OBJECT_TYPE};

        // methods
        private static final String START_METHOD = "startOn";
        private static final String STOP_WITH_EXCEPTION_METHOD = "stopWithException";
        private static final String STOP_METHOD = "stop";
        private static final String VALUE_OF = "valueOf";

        private static final Map<Type, Primitive> PRIMITIVES = new HashMap<Type, Primitive>();
        static {
            final Type shortType = Type.getType(short.class);
            PRIMITIVES.put(shortType, new Primitive(Type.getType(Short.class), shortType));
            final Type intType = Type.getType(int.class);
            PRIMITIVES.put(intType, new Primitive(Type.getType(Integer.class), intType));
            final Type longType = Type.getType(long.class);
            PRIMITIVES.put(longType, new Primitive(Type.getType(Long.class), longType));
            final Type charType = Type.getType(char.class);
            PRIMITIVES.put(charType, new Primitive(Type.getType(Character.class), charType));
            final Type floatType = Type.getType(float.class);
            PRIMITIVES.put(floatType, new Primitive(Type.getType(Float.class), floatType));
            final Type doubleType = Type.getType(double.class);
            PRIMITIVES.put(doubleType, new Primitive(Type.getType(Double.class), doubleType));
            final Type boolType = Type.getType(boolean.class);
            PRIMITIVES.put(boolType, new Primitive(Type.getType(Boolean.class), boolType));
            final Type byteType = Type.getType(byte.class);
            PRIMITIVES.put(boolType, new Primitive(Type.getType(Byte.class), byteType));
        }

        private final boolean isStatic;
        private final Type clazz;
        private final boolean isVoid;
        private final Primitive primitiveWrapper;
        private final Method method;

        public ProxyMethodsVisitor(final MethodVisitor methodVisitor, final int access, final Method method, final Type clazz) {
            super(ASM4, methodVisitor, access, method.getName(), method.getDescriptor());
            this.clazz = clazz;
            this.method = method;
            this.isStatic = Modifier.isStatic(access);

            final Type returnType = method.getReturnType();
            this.isVoid = Type.VOID_TYPE.equals(returnType);
            this.primitiveWrapper = PRIMITIVES.get(returnType);
        }

        @Override
        public void visitCode() {
            final int agentIdx = newLocal(AGENT_CONTEXT);
            getStatic(clazz, method.getName() + FIELD_SUFFIX, KEY_TYPE);
            if (!isStatic) {
                loadThis();
            } else {
                visitInsn(ACONST_NULL); // this == null for static methods
            }
            invokeStatic(AGENT_CONTEXT, new Method(START_METHOD, AGENT_CONTEXT, START_ARGS_TYPES));
            storeLocal(agentIdx);

            final Label tryStart = mark();

            invoke();

            // store result to return it later if it doesn't return void
            final int result = storeResult();

            // take metrics before returning
            loadLocal(agentIdx);

            if (result != -1) {
                loadLocal(result);
                if (primitiveWrapper != null) { // we call agentContext.stop(Object) so we need to wrap primitives
                    invokeStatic(primitiveWrapper.wrapper, primitiveWrapper.method);
                }
            } else {
                visitInsn(ACONST_NULL); // result == null for static methods
            }
            invokeVirtual(AGENT_CONTEXT, new Method(STOP_METHOD, Type.VOID_TYPE, STOP_WITH_OBJECT_ARGS_TYPES));

            // return result
            returnResult(result);

            final Label tryEnd = mark();
            catchException(tryStart, tryEnd, THROWABLE_TYPE);

            // context.stopWithException(throwable);
            final int throwableId = newLocal(THROWABLE_TYPE);
            storeLocal(throwableId);
            loadLocal(agentIdx);
            loadLocal(throwableId);
            invokeVirtual(AGENT_CONTEXT, new Method(STOP_WITH_EXCEPTION_METHOD, Type.VOID_TYPE, STOP_WITH_THROWABLE_ARGS_TYPES));

            // rethrow throwable
            loadLocal(throwableId);
            throwException();

            endMethod();
        }

        private void invoke() {
            final Method mtd = new Method(method.getName() + METHOD_SUFFIX, method.getDescriptor());
            if (isStatic) {
                loadArgs();
                invokeStatic(clazz, mtd);
            } else {
                loadThis();
                loadArgs();
                invokeVirtual(clazz, mtd);
            }
        }

        private int storeResult() {
            final int result;
            if (!isVoid) {
                result = newLocal(method.getReturnType());
                storeLocal(result);
            } else {
                result = -1;
            }
            return result;
        }

        private void returnResult(int result) {
            // return
            if (!isVoid) {
                loadLocal(result);
            }
            returnValue();
        }

        @Override
        public void visitEnd() {
            visitMaxs(0, 0);
            super.visitEnd();
        }

        private static class Primitive {
            private Type wrapper;
            private Method method;

            private Primitive(final Type wrapper, final Type params) {
                this.wrapper = wrapper;
                this.method = new Method(VALUE_OF, wrapper, new Type[] {params});
            }
        }
    }

    // remove annotation from original methods and put it on proxy ones
    private static class MoveAnnotationOnProxy extends MethodVisitor {
        private final MethodVisitor delegate;
        private final ProxyMethodsVisitor decorator;
        private final Collection<Runnable> rewriteTasks = new LinkedList<Runnable>();

        public MoveAnnotationOnProxy(final ProxyMethodsVisitor decorator, final MethodVisitor methodVisitor) {
            super(ASM4);
            this.decorator = decorator;
            this.delegate = methodVisitor;
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            final AnnotationRewriter rewriter = new AnnotationRewriter(rewriteTasks);
            rewriteTasks.add(new Runnable() {
                @Override
                public void run() {
                    rewriter.setDelegate(decorator.visitAnnotation(desc, visible));
                }
            });
            return rewriter;
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
            final AnnotationRewriter rewriter = new AnnotationRewriter(rewriteTasks);
            rewriteTasks.add(new Runnable() {
                @Override
                public void run() {
                    rewriter.setDelegate(decorator.visitParameterAnnotation(parameter, desc, visible));
                }
            });
            return rewriter;
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            final AnnotationRewriter rewriter = new AnnotationRewriter(rewriteTasks);
            rewriteTasks.add(new Runnable() {
                @Override
                public void run() {
                    rewriter.setDelegate(decorator.visitAnnotationDefault());
                }
            });
            return rewriter;
        }

        @Override
        public void visitAttribute(Attribute attr) {
            delegate.visitAttribute(attr);
        }

        @Override
        public void visitCode() {
            delegate.visitCode();
        }

        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
            delegate.visitFrame(type, nLocal, local, nStack, stack);
        }

        @Override
        public void visitInsn(int opcode) {
            delegate.visitInsn(opcode);
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            delegate.visitIntInsn(opcode, operand);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            delegate.visitVarInsn(opcode, var);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            delegate.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            delegate.visitFieldInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            delegate.visitMethodInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
            delegate.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            delegate.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitLabel(Label label) {
            delegate.visitLabel(label);
        }

        @Override
        public void visitLdcInsn(Object cst) {
            delegate.visitLdcInsn(cst);
        }

        @Override
        public void visitIincInsn(int var, int increment) {
            delegate.visitIincInsn(var, increment);
        }

        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
            delegate.visitTableSwitchInsn(min, max, dflt, labels);
        }

        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            delegate.visitLookupSwitchInsn(dflt, keys, labels);
        }

        @Override
        public void visitMultiANewArrayInsn(String desc, int dims) {
            delegate.visitMultiANewArrayInsn(desc, dims);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            delegate.visitTryCatchBlock(start, end, handler, type);
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            delegate.visitLocalVariable(name, desc, signature, start, end, index);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            delegate.visitLineNumber(line, start);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            delegate.visitMaxs(maxStack, maxLocals);
        }

        @Override
        public void visitEnd() {
            delegate.visitEnd();

            // generate proxy
            for (final Runnable visitor : rewriteTasks) {
                visitor.run();
            }
            decorator.visitCode();
            decorator.visitEnd();
        }
    }

    private static class AnnotationRewriter extends AnnotationVisitor {
        private final Collection<Runnable> runnables;
        private AnnotationVisitor delegate;

        public AnnotationRewriter(final Collection<Runnable> tasks) {
            super(ASM4);
            this.runnables = tasks;
        }

        @Override
        public void visit(final String name, final Object value) {
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    delegate.visit(name, value);
                }
            });
        }

        @Override
        public void visitEnum(final String name, final String desc, final String value) {
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    delegate.visitEnum(name, desc, value);
                }
            });
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String name, final String desc) {
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    delegate.visitAnnotation(name, desc);
                }
            });
            return this;
        }

        @Override
        public AnnotationVisitor visitArray(final String name) {
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    delegate.visitArray(name);
                }
            });
            return this;
        }

        @Override
        public void visitEnd() {
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    delegate.visitEnd();
                }
            });
        }

        public void setDelegate(final AnnotationVisitor delegate) {
            this.delegate = delegate;
        }
    }

    // fast visitor used first to get static fields to generate
    public static class SironaKeyVisitor extends ClassVisitor implements Opcodes {
        private final String javaName;
        private final Map<String, String> keys = new HashMap<String, String>();

        public SironaKeyVisitor(final String javaName) {
            super(ASM4, null);
            this.javaName = javaName;
        }

        @Override
        public MethodVisitor visitMethod(int access, final String name, final String desc, final String signature, final String[] exceptions) {
            if (!isSironable(access, name)) {
                return null;
            }

            final String label = javaName.replace("/", ".") + "." + name;
            if (AgentContext.listeners(label) != null) {
                // generate "proxy" method and store the associated field for counter key (generated at the end)
                final String fieldName = name + FIELD_SUFFIX;
                if (!keys.containsKey(fieldName)) {
                    keys.put(fieldName, label);
                }
            }
            return null;
        }

        public boolean hasAdviced() {
            return !keys.isEmpty();
        }

        public Map<String, String> getKeys() {
            return keys;
        }
    }

    // fork from org.objectweb.asm.commons.StaticInitMerger to force our contants to be first
    private static class SironaStaticInitMerger extends ClassVisitor {
        private final Map<String, String> keys;

        private String name;
        private MethodVisitor clinit;
        private int counter;

        protected SironaStaticInitMerger(final ClassVisitor cv, final Map<String, String> keys) {
            super(ASM4, cv);
            this.keys = keys;
        }

        @Override
        public void visit(final int version, final int access, final String name,
                          final String signature, final String superName,
                          final String[] interfaces) {
            cv.visit(version, access, name, signature, superName, interfaces);
            this.name = name;
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name,
                                         final String desc, final String signature, final String[] exceptions) {
            if (STATIC_INIT.equals(name)) {
                final String n = STATIC_CLINT_MERGE_PREFIX + counter++;
                final MethodVisitor mv = cv.visitMethod(ACC_PRIVATE + ACC_STATIC, n, desc, signature, exceptions);
                clinit.visitMethodInsn(INVOKESTATIC, this.name, n, desc);
                return mv;
            }
            return cv.visitMethod(access, name, desc, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            clinit.visitInsn(Opcodes.RETURN);
            clinit.visitMaxs(0, 0);
            clinit.visitEnd();

            cv.visitEnd();
        }

        public void initSironaFields(final Type classType) {
            // generate keys first
            for (final String key : keys.keySet()) {
                visitField(CONSTANT_ACCESS, key, KEY_TYPE.getDescriptor(), null, null).visitEnd();
            }

            // init them in static block
            clinit = cv.visitMethod(ACC_PRIVATE + ACC_STATIC, STATIC_INIT, NO_PARAM_RETURN_VOID, null, null);
            for (final Map.Entry<String, String> key : keys.entrySet()) {
                clinit.visitLdcInsn(key.getValue());
                clinit.visitFieldInsn(PUTSTATIC, classType.getInternalName(), key.getKey(), KEY_TYPE.getDescriptor());
            }
        }
    }
}
