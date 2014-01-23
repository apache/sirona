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

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.StaticInitMerger;

import java.lang.reflect.Modifier;
import java.util.HashMap;
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
    private final Map<String, String> keys = new HashMap<String, String>();
    private Type classType;

    public SironaClassVisitor(final ClassWriter writer, final String javaName) {
        super(ASM4, new StaticInitMerger(STATIC_CLINT_MERGE_PREFIX, writer));
        this.javaName = javaName;
    }

    @Override
    public void visit(final int version, final int access, final String name,
                      final String signature, final String superName, final String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        classType = Type.getType("L" + name.replace('.', '/') + ";");
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
            { // generate "proxy" method and store the associated field for counter key (generated at the end)
                final String fieldName = name + FIELD_SUFFIX;
                if (!keys.containsKey(fieldName)) {
                    keys.put(fieldName, label);
                }

                final ProxyMethodsVisitor sironaVisitor = new ProxyMethodsVisitor(visitor, access, new Method(name, desc), classType);
                sironaVisitor.visitCode();
                sironaVisitor.visitEnd();
            }

            // generate internal method - the proxy (previous one) delegates to this one
            return super.visitMethod(forcePrivate(access), name + METHOD_SUFFIX, desc, signature, exceptions);
        }
        return visitor;
    }

    @Override
    public void visitEnd() {
        if (hasAdviced()) {
            for (final String key : keys.keySet()) {
                visitField(CONSTANT_ACCESS, key, KEY_TYPE.getDescriptor(), null, null).visitEnd();
            }

            final AddConstantsFieldVisitor visitor = new AddConstantsFieldVisitor(super.visitMethod(ACC_STATIC, STATIC_INIT, NO_PARAM_RETURN_VOID, null, null), classType, keys);
            visitor.visitCode();
            visitor.visitInsn(RETURN);
            visitor.visitMaxs(0, 0);
            visitor.visitEnd();
        }

        super.visitEnd();
    }

    public boolean hasAdviced() {
        return !keys.isEmpty();
    }

    private static int forcePrivate(final int access) {
        return (access & ~(Modifier.PRIVATE | Modifier.PUBLIC | Modifier.PROTECTED)) | Modifier.PRIVATE;
    }

    private static boolean isSironable(final int access, final String name) {
        return !name.equals(STATIC_INIT) &&
            !name.equals(CONSTRUCTOR)
            && !Modifier.isAbstract(access) && !Modifier.isNative(access);
    }

    private static class AddConstantsFieldVisitor extends GeneratorAdapter {
        private final Map<String, String> keys;
        private final Type clazz;

        public AddConstantsFieldVisitor(final MethodVisitor methodVisitor, final Type classType, final Map<String, String> keys) {
            super(ASM4, methodVisitor, ACC_STATIC, STATIC_INIT, NO_PARAM_RETURN_VOID);
            this.keys = keys;
            this.clazz = classType;
        }

        @Override
        public void visitCode() {
            super.visitCode();

            for (final Map.Entry<String, String> key : keys.entrySet()) {
                push(key.getValue());
                putStatic(clazz, key.getKey(), KEY_TYPE);
            }
        }
    }

    public String getJavaName()
    {
        return javaName;
    }

    public Type getClassType()
    {
        return classType;
    }

    private static class ProxyMethodsVisitor extends GeneratorAdapter {
        private static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
        private static final Type[] STOP_WITH_THROWABLE_ARGS_TYPES = new Type[]{ THROWABLE_TYPE };
        private static final Type OBJECT_TYPE = Type.getType(Object.class);
        private static final Type[] STOP_WITH_OBJECT_ARGS_TYPES = new Type[]{ OBJECT_TYPE };
        private static final Type[] START_ARGS_TYPES = new Type[]{ KEY_TYPE, OBJECT_TYPE };

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

        public ProxyMethodsVisitor(final MethodVisitor methodVisitor,
                                   final int access, final Method method, final Type clazz) {
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

        private static class Primitive {
            private Type wrapper;
            private Method method;

            private Primitive(final Type wrapper, final Type params) {
                this.wrapper = wrapper;
                this.method = new Method(VALUE_OF, wrapper, new Type[]{ params });
            }
        }
    }
}
