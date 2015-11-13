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

import org.apache.commons.io.IOUtils;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.AsmAdaptor;
import org.apache.openjpa.enhance.PCClassFileTransformer;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.PersistenceMetaDataFactory;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import serp.bytecode.BCClass;
import serp.bytecode.Project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

public class InJvmTransformerRunner extends BlockJUnit4ClassRunner {
    private final Class<?>[] transformers;
    private ClassLoader testLoader = null;
    private ClassLoader originalLoader = null;
    private static final Boolean DEBUG = Boolean.getBoolean( "sirona.agent.debug" );

    public InJvmTransformerRunner(final Class<?> klass) throws InitializationError {
        super(klass);

        final Transformers customTransformers = klass.getAnnotation(Transformers.class);
        if (customTransformers != null) {
            transformers = customTransformers.value();
        } else {
            transformers = new Class[]{SironaTransformer.class};
        }

        Thread.currentThread().setContextClassLoader(getTestLoader());
        try {
            final Class<?> testTransformedClass = testLoader.loadClass(getTestClass().getName());
            Field field;
            try { // junit 4.11
                field = ParentRunner.class.getDeclaredField("fTestClass");
            } catch (final NoSuchFieldException nsfe) { // junit 4.12
                field = ParentRunner.class.getDeclaredField("testClass");
            }
            field.setAccessible(true);
            field.set(this, new TestClass(testTransformedClass));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
    }

    private ClassLoader getTestLoader() {
        if (testLoader == null) {
            originalLoader = Thread.currentThread().getContextClassLoader();
            testLoader = new SironaClassLoader(getTestClass().getJavaClass(), transformers, originalLoader);
        }
        return testLoader;
    }

    @Override
    protected Statement withBeforeClasses(final Statement statement) {
        final Statement beforeClasses = super.withBeforeClasses(statement);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Thread.currentThread().setContextClassLoader(testLoader);
                beforeClasses.evaluate();
            }
        };
    }

    @Override
    protected Statement withAfterClasses(final Statement statement) {
        final Statement afterClasses = super.withAfterClasses(statement);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    afterClasses.evaluate();
                } finally {
                    Thread.currentThread().setContextClassLoader(originalLoader);
                }
            }
        };
    }

    public static class SironaClassLoader extends URLClassLoader {
        private final Class<?>[] transformers;
        private final Class<?> testClass;

        public SironaClassLoader(final URL[] urls, final Class<?> testClass, final Class<?>[] transformers, final ClassLoader parent) {
            super(urls, parent);
            this.testClass = testClass;
            this.transformers = transformers;
        }

        public SironaClassLoader(final Class<?> testClass, final Class<?>[] transformers, final ClassLoader parent) {
            super(new URL[0], parent);
            this.testClass = testClass;
            this.transformers = transformers;
        }

        @Override
        public String toString() {
            return InJvmTransformerRunner.class.getSimpleName() + "-" + super.toString();
        }

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            if (!name.startsWith(testClass.getName())) {
                return getParent().loadClass(name);
            }

            final Class<?> existing = findLoadedClass(name);
            if (existing != null) {
                return existing;
            }

            try {
                final String resourceName = name.replaceAll("\\.", "/") + ".class";
                final InputStream is = getResourceAsStream(resourceName);
                if (is == null) {
                    throw new ClassNotFoundException(name);
                }

                final String className = resourceName.replace(".class", "");

                byte[] buffer = IOUtils.toByteArray(is);
                for (final Class<?> t : transformers) {
                    if (SironaTransformer.class.equals(t)) {
                        final SironaTransformer transformer = new SironaTransformer(false, false, null);
                        buffer = transformer.transform(this, className, null, null, buffer);
                    } else if (PCClassFileTransformer.class.equals(t)) {
                        buffer = handleJpa(name, buffer);
                    } else {
                        buffer = ClassFileTransformer.class.cast(t.newInstance()).transform(this, className, null, null, buffer);
                    }

                    if (DEBUG) {
                        final File dump = new File(System.getProperty("java.io.tmpdir"), "sirona-dump/" + className + ".class");
                        dump.getParentFile().mkdirs();

                        FileOutputStream w = null;
                        try {
                            w = new FileOutputStream(dump);
                            w.write(buffer);
                        } finally {
                            if (w != null) {
                                w.close();
                            }
                        }
                    }

                }
                return defineClass(name, buffer, 0, buffer.length);
            } catch (final Throwable t) {
                throw new ClassNotFoundException(t.getMessage(), t);
            }
        }

        private byte[] handleJpa(final String name, final byte[] buffer) throws IOException {
            return Jpa.handleJpa(name, buffer, this);
        }
    }

    private static class Jpa {
        private Jpa() {
            // no-op
        }

        public static byte[] handleJpa(final String name, final byte[] buffer, final ClassLoader loader) throws IOException {
            if (name.endsWith("Entity")) {
                // hacky but avoid to build a full openjpa project/context
                final PersistenceMetaDataFactory factory = new PersistenceMetaDataFactory();
                factory.setTypes("org.apache.test.sirona.javaagent.OpenJPATest$ServiceSquareEntity");

                final MetaDataRepository repos = new MetaDataRepository();
                repos.setConfiguration(new OpenJPAConfigurationImpl());
                repos.setMetaDataFactory(factory);

                final BCClass type = new Project().loadClass(new ByteArrayInputStream(buffer), new URLClassLoader(new URL[0], loader.getParent()));
                final PCEnhancer enhancer = new PCEnhancer(repos.getConfiguration(), type, repos, loader);
                enhancer.setAddDefaultConstructor(true);
                enhancer.setEnforcePropertyRestrictions(true);

                if (enhancer.run() != PCEnhancer.ENHANCE_NONE) {
                    final BCClass pcb = enhancer.getPCBytecode();
                    final byte[] transformed = AsmAdaptor.toByteArray(pcb, pcb.toByteArray());
                    if (transformed != null) {
                        return transformed;
                    }
                }
            }
            return buffer;
        }
    }
}
