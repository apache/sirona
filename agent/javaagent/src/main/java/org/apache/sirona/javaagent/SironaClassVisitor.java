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

import org.apache.sirona.aop.AbstractPerformanceInterceptor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.reflect.Modifier;

public class SironaClassVisitor extends ClassVisitor implements Opcodes {
    private static final String STATIC_INIT = "<clinit>";
    private static final String CONSTRUCTOR = "<init>";
    private static final String METHOD_SUFFIX = "_$_$irona_$_internal_$_original_$_";

    private final String javaName;
    private Type classType;

    public SironaClassVisitor(final ClassWriter writer, final String javaName) {
        super(ASM4, writer);
        this.javaName = javaName;
    }

    @Override
    public void visit(final int version, final int access, final String name,
                      final String signature, final String superName, final String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        classType = Type.getType("L" + name.replace('.', '/') + ";");
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (!isSironable(access, name)) {
            return methodVisitor;
        }

        final String label = javaName.replace("/", ".") + "." + name;
        AgentPerformanceInterceptor.initKey(label);

        { // generate "proxy" method
            final SironaMethodVisitor sironaVisitor = new SironaMethodVisitor(methodVisitor, label, access, new Method(name, desc), classType);
            sironaVisitor.visitCode();
            sironaVisitor.visitEnd();
        }

        // generate internal method - the proxy (previous one) delegates to this one
        return super.visitMethod(forcePrivate(access), name + METHOD_SUFFIX, desc, signature, exceptions);
    }

    private int forcePrivate(final int access) {
        return (access & ~(Modifier.PRIVATE | Modifier.PUBLIC | Modifier.PROTECTED)) | Modifier.PRIVATE;
    }

    private boolean isSironable(final int access, final String name) {
        return !name.equals(STATIC_INIT) && !name.equals(CONSTRUCTOR)
            && !Modifier.isAbstract(access) && !Modifier.isNative(access);
    }

    private static class SironaMethodVisitor extends GeneratorAdapter {
        // types
        private static final Type AGENT_COUNTER = Type.getType(AgentPerformanceInterceptor.class);
        private static final Type CONTEXT_TYPE = Type.getType(AbstractPerformanceInterceptor.Context.class);
        private static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
        private static final Type STRING_TYPE = Type.getType(String.class);
        private static final String CONTEXT_NAME = CONTEXT_TYPE.getDescriptor();

        // methods
        private static final String START_METHOD = "start";
        private static final String STOP_WITH_EXCEPTION_METHOD = "stopWithException";
        private static final String STOP_METHOD = "stop";

        private final String label;
        private final boolean isStatic;
        private final Type clazz;
        private final boolean isVoid;
        private final Method method;

        public SironaMethodVisitor(final MethodVisitor methodVisitor, final String label,
                                   final int access, final Method method, final Type clazz) {
            super(ASM4, methodVisitor, access, method.getName(), method.getDescriptor());
            this.label = label;
            this.clazz = clazz;
            this.method = method;
            this.isStatic = Modifier.isStatic(access);
            this.isVoid = Type.VOID_TYPE.equals(method.getReturnType());
        }

        @Override
        public void visitCode() {
            final int agentIdx = newLocal(CONTEXT_TYPE);
            push(label);
            invokeStatic(AGENT_COUNTER, new Method(START_METHOD, "(" + STRING_TYPE + ")" + CONTEXT_NAME));
            storeLocal(agentIdx);

            final Label tryStart = mark();

            invoke();

            // store result to return it later if it doesn't return void
            final int result = storeResult();

            // take metrics before returning
            loadLocal(agentIdx);
            invokeVirtual(CONTEXT_TYPE, new Method(STOP_METHOD, "()V"));

            // return result
            returnResult(result);

            final Label tryEnd = mark();
            catchException(tryStart, tryEnd, THROWABLE_TYPE);

            // context.stopWithException(throwable);
            final int throwableId = newLocal(THROWABLE_TYPE);
            storeLocal(throwableId);
            loadLocal(agentIdx);
            loadLocal(throwableId);
            invokeVirtual(CONTEXT_TYPE, new Method(STOP_WITH_EXCEPTION_METHOD, Type.getType(Void.TYPE), new Type[]{THROWABLE_TYPE}));

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
    }
}
