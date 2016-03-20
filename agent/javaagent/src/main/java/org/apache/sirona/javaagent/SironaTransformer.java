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

import org.apache.sirona.javaagent.classloader.LoadFirstClassLoader;
import org.apache.sirona.javaagent.logging.SironaAgentLogging;
import org.apache.sirona.util.ClassLoaders;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SironaTransformer implements ClassFileTransformer {
    private static final String DELEGATING_CLASS_LOADER = "sun.reflect.DelegatingClassLoader";

    // used to not load classes while loading and create linkage errors
    private final ConcurrentMap<ClassLoader, ClassLoader> tempClassLoaders = new ConcurrentHashMap<ClassLoader, ClassLoader>();

    private final boolean debug;
    private final String[] autoClassLoaderExcludes;
    private final boolean skipTempLoader;

    public SironaTransformer(final boolean debug, final boolean skipTempLoader, final String tempClassLoaders) {
        this.debug = debug || Boolean.getBoolean("sirona.javaagent.debug");
        this.skipTempLoader = skipTempLoader || Boolean.getBoolean("sirona.javaagent.skipTempLoader");

        final String excludes = System.getProperty(
                "sirona.javaagent.dontAutoClassLoaderExclude",
                tempClassLoaders != null ?
                        tempClassLoaders :
                        "org.apache.openjpa.lib.util.TemporaryClassLoader,org.apache.openejb.core.TempClassLoader");
        this.autoClassLoaderExcludes = excludes.split(" *, *");
    }

    public void evictClassLoaders() { // we will recreate them if needed
        tempClassLoaders.clear();
    }

    @Override
    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
        if (shouldTransform(className, loader) && !isExcludedLoader(loader)) {
            return doTransform(className, classfileBuffer);
        }
        return classfileBuffer;
    }

    private boolean isExcludedLoader(final ClassLoader loader) {
        if (loader == null) {
            return false;
        }

        final String name = loader.getClass().getName();
        for (int i = 0; i < autoClassLoaderExcludes.length; i++) {
            if (autoClassLoaderExcludes[i].equals(name)) {
                return true;
            }
        }
        return LoadFirstClassLoader.class.getName().equals(name); // of course we exclude our internal loader
    }

    protected byte[] doTransform(final String className, final byte[] classfileBuffer) {
        try {
            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new SironaClassWriter(className == null ? null : className.replace('/', '.'),
                    skipTempLoader ? null : tempClassLoaders, reader, ClassWriter.COMPUTE_FRAMES);
            final SironaClassVisitor advisor = new SironaClassVisitor(writer, className, classfileBuffer);
            reader.accept(advisor, ClassReader.SKIP_FRAMES);

            if (advisor.wasAdviced()) {
                final byte[] bytes = writer.toByteArray();
                if (debug) {
                    final File dump = new File(System.getProperty("java.io.tmpdir"), "sirona-dump/" + className + ".class");
                    dump.getParentFile().mkdirs();

                    FileOutputStream w = null;
                    try {
                        w = new FileOutputStream(dump);
                        w.write(bytes);
                    } finally {
                        if (w != null) {
                            w.close();
                        }
                    }
                }
                return bytes;
            }
            return classfileBuffer;
        } catch (final Throwable e) {
            if (SironaAgentLogging.AGENT_DEBUG) {
                SironaAgentLogging.debug("fail transforming class {0} : {1}", className, e.getMessage());
                e.printStackTrace();
            }
            //throw new RuntimeException( e.getMessage(), e );
            System.out.println("fail to transform class:" + className + ", " + e.getMessage());
            e.printStackTrace();
            return classfileBuffer;
        }
    }

    public static class SironaClassWriter extends ClassWriter {
        private final ConcurrentMap<ClassLoader, ClassLoader> tempClassLoaders;
        private final String currentClass;

        public SironaClassWriter(final String currentClass,
                                 final ConcurrentMap<ClassLoader, ClassLoader> tempClassLoaders,
                                 final ClassReader classReader, final int flags) {
            super(classReader, flags);
            this.currentClass = currentClass;
            this.tempClassLoaders = tempClassLoaders;
        }

        /**
         * copy paste code from asm as we need a different way to load classes
         *
         * @param type1
         * @param type2
         * @return
         */
        @Override
        protected String getCommonSuperClass(final String type1, final String type2) {
            final ClassLoader loader = createTempLoader();
            Class<?> c, d;
            try {
                c = findClass(loader, type1.replace('/', '.'));
                d = findClass(loader, type2.replace('/', '.'));
            } catch (final Exception e) {
                throw new RuntimeException(e.toString());
            } catch (final ClassCircularityError e) {
                return "java/lang/Object";
            }
            if (c.isAssignableFrom(d)) {
                return type1;
            }
            if (d.isAssignableFrom(c)) {
                return type2;
            }
            if (c.isInterface() || d.isInterface()) {
                return "java/lang/Object";
            } else {
                do {
                    c = c.getSuperclass();
                } while (!c.isAssignableFrom(d));
                return c.getName().replace('.', '/');
            }
        }

        private ClassLoader createTempLoader() {
            final ClassLoader tccl = ClassLoaders.current();
            if (tempClassLoaders != null) {
                ClassLoader temp = tempClassLoaders.get(tccl);
                if (temp == null) {
                    temp = new LoadFirstClassLoader(tccl);
                    final ClassLoader existing = tempClassLoaders.putIfAbsent(tccl, temp);
                    if (existing != null) {
                        temp = existing;
                    }
                }
                return temp;
            }
            return tccl;
        }

        protected Class<?> findClass(final ClassLoader tccl, final String className)
                throws ClassNotFoundException {
            try {
                return currentClass.equals(className) ? Object.class : Class.forName(className, false, tccl);
            } catch (ClassNotFoundException e) {
                return Class.forName(className, false, getClass().getClassLoader());
            }
        }
    }

    protected boolean shouldTransform(final String className, final ClassLoader loader) {
        return !(className == null // framework with bug
                || (loader != null && loader.getClass().getName().equals(DELEGATING_CLASS_LOADER))
                || className.startsWith("sun/reflect")
                || className.startsWith("com/sun/proxy")
                || className.startsWith("org/apache/sirona"));
    }
}
