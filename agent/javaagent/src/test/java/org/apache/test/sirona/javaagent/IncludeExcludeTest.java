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
package org.apache.test.sirona.javaagent;

import org.apache.sirona.javaagent.SironaAgent;
import org.junit.Test;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class IncludeExcludeTest {
    @Test
    public void include() throws IllegalClassFormatException {
        final InstrumentationForTestPurpose instrumentation = new InstrumentationForTestPurpose();
        SironaAgent.agentmain("includes=prefix:org.foo", instrumentation);
        final ClassFileTransformer transformer = instrumentation.getTransformer();
        assertNotNull(transformer);

        try {
            transformer.transform(getClass().getClassLoader(), "org/foo", null, null, null);
            fail("should fail since we retransform org/foo");
        } catch (final NullPointerException npe) {
            // ok
        }
        transformer.transform(getClass().getClassLoader(), "org/bar", null, null, null); // not included
    }

    @Test
    public void exclude() throws IllegalClassFormatException {
        final InstrumentationForTestPurpose instrumentation = new InstrumentationForTestPurpose();
        SironaAgent.agentmain("excludes=prefix:org.foo", instrumentation);
        final ClassFileTransformer transformer = instrumentation.getTransformer();
        assertNotNull(transformer);

        try {
            transformer.transform(getClass().getClassLoader(), "org/bar", null, null, null);
            fail("should fail since we transform org/bar");
        } catch (final NullPointerException npe) {
            // ok
        }
        transformer.transform(getClass().getClassLoader(), "org/foo", null, null, null);
    }

    @Test
    public void includeExclude() throws IllegalClassFormatException {
        final InstrumentationForTestPurpose instrumentation = new InstrumentationForTestPurpose();
        SironaAgent.agentmain("includes=prefix:org.foo|excludes=prefix:org.foo.bar", instrumentation);
        final ClassFileTransformer transformer = instrumentation.getTransformer();
        assertNotNull(transformer);

        try {
            transformer.transform(getClass().getClassLoader(), "org/foo", null, null, null);
            fail("should fail since we transform org/bar");
        } catch (final NullPointerException npe) {
            // ok
        }
        transformer.transform(getClass().getClassLoader(), "org/foo/bar/dummy", null, null, null);
    }

    @Test // include all what is possible
    public void defaultConfig() throws IllegalClassFormatException {
        final InstrumentationForTestPurpose instrumentation = new InstrumentationForTestPurpose();
        SironaAgent.agentmain(null, instrumentation);
        final ClassFileTransformer transformer = instrumentation.getTransformer();
        assertNotNull(transformer);
        try {
            transformer.transform(getClass().getClassLoader(), "org/foo", null, null, null);
            fail("should fail since we transform org/bar");
        } catch (final NullPointerException npe) {
            // ok
        }
        try {
            transformer.transform(getClass().getClassLoader(), "org/bar", null, null, null);
            fail("should fail since we transform org/bar");
        } catch (final NullPointerException npe) {
            // ok
        }
        try {
            transformer.transform(getClass().getClassLoader(), "com/sun/dumy", null, null, null);
            fail("should fail since we transform org/bar");
        } catch (final NullPointerException npe) {
            // ok
        }
    }

    public static class InstrumentationForTestPurpose implements Instrumentation {
        private ClassFileTransformer transformer;

        public ClassFileTransformer getTransformer() {
            return transformer;
        }

        @Override
        public void addTransformer(final ClassFileTransformer transformer, final boolean canRetransform) {
            addTransformer(transformer);
        }

        @Override
        public void addTransformer(final ClassFileTransformer transformer) {
            this.transformer = transformer;
        }

        @Override
        public boolean removeTransformer(final ClassFileTransformer transformer) {
            return false;
        }

        @Override
        public boolean isRetransformClassesSupported() {
            return false;
        }

        @Override
        public void retransformClasses(final Class<?>... classes) throws UnmodifiableClassException {
            // no-op
        }

        @Override
        public boolean isRedefineClassesSupported() {
            return false;
        }

        @Override
        public void redefineClasses(final ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {
            // no-op
        }

        @Override
        public boolean isModifiableClass(final Class<?> theClass) {
            return false;
        }

        @Override
        public Class[] getAllLoadedClasses() {
            return new Class[0];
        }

        @Override
        public Class[] getInitiatedClasses(final ClassLoader loader) {
            return new Class[0];
        }

        @Override
        public long getObjectSize(final Object objectToSize) {
            return 0;
        }

        @Override
        public void appendToBootstrapClassLoaderSearch(final JarFile jarfile) {
            // no-op
        }

        @Override
        public void appendToSystemClassLoaderSearch(final JarFile jarfile) {
            // no-op
        }

        @Override
        public boolean isNativeMethodPrefixSupported() {
            return false;
        }

        @Override
        public void setNativeMethodPrefix(final ClassFileTransformer transformer, final String prefix) {
            // no-op
        }
    }
}
