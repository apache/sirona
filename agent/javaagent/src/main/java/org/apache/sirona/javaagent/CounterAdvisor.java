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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * TODO:
 * 1) create in transformed class constant for Counter.Keys
 * 2) avoid AgentCounter usage
 */
public class CounterAdvisor extends ClassVisitor implements Opcodes {
    public static final String AGENT_COUNTER = AgentCounter.class.getName().replace(".", "/");
    private final String javaName;
    private boolean isInterface;

    public CounterAdvisor(final ClassWriter writer, final String javaName) {
        super(ASM4, writer);
        this.javaName = javaName;
    }

    @Override
    public void visit(final int version, final int access, final String name,
                      final String signature, final String superName, final String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        isInterface = (access & ACC_INTERFACE) != 0;
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor delegate = cv.visitMethod(access, name, desc, signature, exceptions);
        if (!isInterface) {
            final String label = label(javaName, name, desc, exceptions);
            return new SironaMethodVisitor(delegate, label, access, name, desc);
        }
        return delegate;
    }

    private String label(final String className, final String methodName, final String desc, final String[] ex) {
        return className.replace("/", ".") + "." + methodName;
    }

    private static class SironaMethodVisitor extends AdviceAdapter {
        private final String label;
        private int agentIdx;

        public SironaMethodVisitor(final MethodVisitor methodVisitor, final String label,
                                    final int access, final String name, final String desc) {
            super(ASM4, methodVisitor, access, name, desc);
            this.label = label;
        }

        @Override
        public void onMethodEnter() {
            agentIdx = newLocal(Type.getType(AgentCounter.class));
            mv.visitCode();
            mv.visitLdcInsn(label);
            mv.visitMethodInsn(INVOKESTATIC, AGENT_COUNTER, "start", "(Ljava/lang/String;)L" + AGENT_COUNTER + ";");
            mv.visitVarInsn(ASTORE, agentIdx);
        }

        @Override
        public void onMethodExit(final int opcode) {
            mv.visitVarInsn(ALOAD, agentIdx);
            mv.visitMethodInsn(INVOKEVIRTUAL, AGENT_COUNTER, "stop", "()V");
        }
    }
}
