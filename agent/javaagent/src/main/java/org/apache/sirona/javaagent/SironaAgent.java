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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.jar.JarFile;

public class SironaAgent {
    private static final Collection<String> JVM_ENHANCED = Arrays.asList(
        "sun/net/www/protocol/http/HttpURLConnection"
    );

    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        agentmain(agentArgs, instrumentation);
    }

    // all is done by reflection cause we change classloader to be able to enhance JVM too
    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            final String resource = SironaAgent.class.getName().replace('.', '/') + ".class";
            final URL agentUrl = loader.getResource(resource);
            if (agentUrl != null) {
                final String file = agentUrl.getFile();
                final int endIndex = file.indexOf('!');
                if (endIndex > 0) {
                    final String realPath = decode(new URL(file.substring(0, endIndex)).getFile());
                    instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(realPath));
                } // else javaagent not set on the JVM so ignoring appendToSystemClassLoaderSearch
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final String libs = extractConfig(agentArgs, "libs=");
        if (libs != null) {
            final File root = new File(libs);
            if (root.exists()) {
                final File[] children = root.listFiles();
                if (children != null) {
                    for (final File f : children) {
                        try {
                            instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(f));
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        try { // eager init
            loader.loadClass("org.apache.sirona.javaagent.AgentContext")
                    .getMethod("touch").invoke(null);
            loader.loadClass("org.apache.sirona.configuration.Configuration")
                    .getMethod("is", String.class, boolean.class).invoke(null, "", true);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        try {
            instrumentation.addTransformer(
                    ClassFileTransformer.class.cast(loader.loadClass("org.apache.sirona.javaagent.SironaAgent$SironaTransformer")
                        .getConstructor(String.class).newInstance(agentArgs)),
                    instrumentation.isRetransformClassesSupported());

            for (final String jvm : JVM_ENHANCED) {
                instrumentation.retransformClasses(loader.loadClass(jvm.replace('/', '.')));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }


    private SironaAgent() {
        // no-op
    }

    private static String decode(final String fileName) {
        if (fileName.indexOf('%') == -1) {
            return fileName;
        }

        final StringBuilder result = new StringBuilder(fileName.length());
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < fileName.length();) {
            final char c = fileName.charAt(i);
            if (c == '%') {
                out.reset();
                do {
                    if (i + 2 >= fileName.length()) {
                        throw new IllegalArgumentException("Incomplete % sequence at: " + i);
                    }

                    int d1 = Character.digit(fileName.charAt(i + 1), 16);
                    int d2 = Character.digit(fileName.charAt(i + 2), 16);

                    if (d1 == -1 || d2 == -1) {
                        throw new IllegalArgumentException("Invalid % sequence (" + fileName.substring(i, i + 3) + ") at: " + String.valueOf(i));
                    }
                    out.write((byte) ((d1 << 4) + d2));
                    i += 3;
                } while (i < fileName.length() && fileName.charAt(i) == '%');
                result.append(out.toString());
            } else {
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }

    private static String extractConfig(final String agentArgs, final String startStr) {
        if (agentArgs != null && agentArgs.contains(startStr)) {
            final int start = agentArgs.indexOf(startStr) + startStr.length();
            final int separator = agentArgs.indexOf('|', start);
            final int endIdx;
            if (separator > 0) {
                endIdx = separator;
            } else {
                endIdx = agentArgs.length();
            }
            return agentArgs.substring(start, endIdx);
        }
        return null;
    }

    public static class SironaTransformer implements ClassFileTransformer {
        private static final String DELEGATING_CLASS_LOADER = "sun.reflect.DelegatingClassLoader";

        private final PredicateEvaluator includeEvaluator;
        private final PredicateEvaluator excludeEvaluator;

        // used by reflection so don't change visibility without testing
        public SironaTransformer(final String agentArgs) {
            includeEvaluator = createEvaluator(agentArgs, "includes=", new PredicateEvaluator("true:true", ","));
            excludeEvaluator = createEvaluator(agentArgs, "excludes=", new PredicateEvaluator(null, null)); // no matching
        }

        private PredicateEvaluator createEvaluator(final String agentArgs, final String str, final PredicateEvaluator defaultEvaluator) {
            final String configuration = extractConfig(agentArgs, str);
            if (configuration != null) {
                return new PredicateEvaluator(configuration, ",");
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
            try {
                final ClassReader reader = new ClassReader(classfileBuffer);
                final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
                final SironaClassVisitor advisor = new SironaClassVisitor(writer, className);
                reader.accept(advisor, ClassReader.SKIP_DEBUG);
                return writer.toByteArray();
            } catch (final RuntimeException re) {
                if (Boolean.getBoolean("sirona.agent.debug")) {
                    re.printStackTrace();
                }
                throw re;
            }
        }

        private boolean shouldTransform(final String className, final ClassLoader loader) {
            return JVM_ENHANCED.contains(className)
                || !(loader == null // bootstrap classloader
                    || className == null // framework with bug
                    || loader.getClass().getName().equals(DELEGATING_CLASS_LOADER)
                    || className.startsWith("sun/reflect")
                    || className.startsWith("com/sun/proxy")
                    || className.startsWith("org/apache/sirona"))

                    && includeEvaluator.matches(className.replace("/", "."))
                    && !excludeEvaluator.matches(className.replace("/", "."));
        }
    }
}
