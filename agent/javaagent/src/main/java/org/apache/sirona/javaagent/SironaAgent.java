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

import org.apache.sirona.configuration.Configuration;
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
        AgentContext.touch(); // important otherwise we can get NoClassDefFound if initialized lazily
        Configuration.is("", true); // a touch to force eager init

        instrumentation.addTransformer(new SironaTransformer(agentArgs), true);
    }


    private SironaAgent() {
        // no-op
    }

    private static class SironaTransformer implements ClassFileTransformer {
        private static final String DELEGATING_CLASS_LOADER = "sun.reflect.DelegatingClassLoader";

        private final PredicateEvaluator includeEvaluator;
        private final PredicateEvaluator excludeEvaluator;

        private SironaTransformer(final String agentArgs) {
            includeEvaluator = createEvaluator(agentArgs, "includes=", new PredicateEvaluator("true:true", ","));
            excludeEvaluator = createEvaluator(agentArgs, "excludes=", new PredicateEvaluator(null, null)); // no matching
        }

        private PredicateEvaluator createEvaluator(final String agentArgs, final String str, final PredicateEvaluator defaultEvaluator) {
            if (agentArgs != null && agentArgs.contains(str)) {
                final int start = agentArgs.indexOf(str) + str.length();
                final int separator = agentArgs.indexOf('|', start);
                final int endIdx;
                if (separator > 0) {
                    endIdx = separator;
                } else {
                    endIdx = agentArgs.length();
                }
                return new PredicateEvaluator(agentArgs.substring(start, endIdx), ",");
            }
            return defaultEvaluator;
        }

        @Override
        public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
                                final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
            if (shouldTransform(className, loader)) {
                return doTransform(className, classfileBuffer);
            }
            return classfileBuffer;
        }

        private byte[] doTransform(final String className, final byte[] classfileBuffer) {
            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
            final SironaClassVisitor advisor = new SironaClassVisitor(writer, className);
            try {
                reader.accept(advisor, ClassReader.SKIP_DEBUG);
            } catch (final RuntimeException re) {
                re.printStackTrace(); // log it otherwise hard to know if it fails
                throw re;
            }
            return writer.toByteArray();
        }

        private boolean shouldTransform(final String className, final ClassLoader loader) {
            return !(
                           loader == null
                        || className == null
                        || loader.getClass().getName().equals(DELEGATING_CLASS_LOADER)
                        || className.startsWith("org/apache/sirona")
                        || className.startsWith("com/sun/proxy")
                    )
                && includeEvaluator.matches(className.replace("/", "."))
                && !excludeEvaluator.matches(className.replace("/", "."));

        }
    }
}
