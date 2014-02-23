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

import org.apache.sirona.javaagent.logging.SironaAgentLogging;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class SironaTransformer implements ClassFileTransformer {
    private static final String DELEGATING_CLASS_LOADER = "sun.reflect.DelegatingClassLoader";

    private final boolean debug;

    public SironaTransformer(final boolean debug) {
        this.debug = debug;
    }

    @Override
    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
        if (shouldTransform(className, loader)) {
            return doTransform(className, classfileBuffer);
        }
        return classfileBuffer;
    }

    protected byte[] doTransform(final String className, final byte[] classfileBuffer) {
        try {
            final SironaClassVisitor.SironaKeyVisitor keyVisitor = new SironaClassVisitor.SironaKeyVisitor(className);
            new ClassReader(classfileBuffer).accept(keyVisitor, ClassReader.SKIP_DEBUG);
            if (keyVisitor.hasAdviced()) {
                final ClassReader reader = new ClassReader(classfileBuffer);
                final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                final SironaClassVisitor advisor = new SironaClassVisitor(writer, className, keyVisitor.getKeys());
                reader.accept(advisor, ClassReader.SKIP_DEBUG);

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
        } catch (final Exception e) {
            if ( SironaAgentLogging.AGENT_DEBUG) {
                SironaAgentLogging.debug( "fail transforming class {0} : {1}", className, e.getMessage() );
                e.printStackTrace();
            }
            throw new RuntimeException( e.getMessage(), e );
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
