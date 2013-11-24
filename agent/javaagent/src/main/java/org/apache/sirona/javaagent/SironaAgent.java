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

import org.apache.sirona.configuration.predicate.PredicateEvaluator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class SironaAgent {
    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        agentmain(agentArgs, instrumentation);
    }

    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
        instrumentation.addTransformer(new SironaTransformer(agentArgs));
    }


    private SironaAgent() {
        // no-op
    }

    private static class SironaTransformer implements ClassFileTransformer {
        private final PredicateEvaluator evaluator;

        private SironaTransformer(final String agentArgs) {
            final String includeStr = "includes=";
            if (agentArgs != null && agentArgs.contains(includeStr)) {
                final int start = agentArgs.indexOf(includeStr) + includeStr.length();
                evaluator = new PredicateEvaluator(agentArgs.substring(start, Math.max(agentArgs.length(), agentArgs.indexOf('|', start))), ",");
            } else {
                evaluator = new PredicateEvaluator("regex:.*", ",");
            }
        }

        @Override
        public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
                                final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
            if (shouldTransform(className)) {
                return doTransform(className, classfileBuffer);
            }
            return classfileBuffer;
        }

        private byte[] doTransform(final String className, final byte[] classfileBuffer) {
            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
            final CounterAdvisor advisor = new CounterAdvisor(writer, className);
            reader.accept(advisor, ClassReader.SKIP_DEBUG);
            return writer.toByteArray();
        }

        private boolean shouldTransform(final String className) {
            if (className.startsWith("sun/")
                || className.startsWith("com/sun/")
                || className.startsWith("java/")
                || className.startsWith("org/apache/sirona")) {
                return false;
            }

            return evaluator.matches(className.replace("/", "."));
        }
    }
}
