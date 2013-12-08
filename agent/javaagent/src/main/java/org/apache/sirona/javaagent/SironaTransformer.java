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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class SironaTransformer implements ClassFileTransformer {
    private static final String DELEGATING_CLASS_LOADER = "sun.reflect.DelegatingClassLoader";

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
            if (advisor.hasAdviced()) {
                return writer.toByteArray();
            }
            return classfileBuffer;
        } catch (final RuntimeException re) {
            if (Boolean.getBoolean("sirona.agent.debug")) {
                re.printStackTrace();
            }
            throw re;
        }
    }

    private static boolean shouldTransform(final String className, final ClassLoader loader) {
        return !(className == null // framework with bug
                || (loader != null && loader.getClass().getName().equals(DELEGATING_CLASS_LOADER))
                || className.startsWith("sun/reflect")
                || className.startsWith("com/sun/proxy")
                || className.startsWith("org/apache/sirona"));
    }
}
