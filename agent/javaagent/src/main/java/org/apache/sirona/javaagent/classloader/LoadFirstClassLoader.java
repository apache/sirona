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
package org.apache.sirona.javaagent.classloader;

import org.apache.sirona.SironaException;
import org.apache.sirona.util.ClassLoaders;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class LoadFirstClassLoader extends URLClassLoader {
    private final ClassLoader jvmLoader;

    public LoadFirstClassLoader(final ClassLoader parent) {
        super(findUrls(parent), parent);
        jvmLoader = ClassLoader.getSystemClassLoader().getParent();
    }

    private static URL[] findUrls(final ClassLoader loader) {
        try {
            return ClassLoaders.findUrls(loader);
        } catch (IOException e) {
            if (URLClassLoader.class.isInstance(loader)) {
                return URLClassLoader.class.cast(loader).getURLs();
            }
            throw new SironaException(e);
        }
    }

    @Override
    public synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        // synchronized (getClassLoadingLock(name)) { // j7
            Class<?> clazz = findLoadedClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }

            try {
                clazz = jvmLoader.loadClass(name);
                if (clazz != null) {
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (final ClassNotFoundException ignored) {
                // no-op
            }

            // look for it in this classloader
            clazz = loadInternal(name, resolve);
            if (clazz != null) {
                return clazz;
            }

            // finally delegate
            clazz = loadFromParent(name, resolve);
            if (clazz != null) {
                return clazz;
            }

            throw new ClassNotFoundException(name);
        // } // j7
    }

    private Class<?> loadFromParent(final String name, final boolean resolve) {
        ClassLoader parent = getParent();
        if (parent == null) {
            parent = jvmLoader;
        }
        try {
            final Class<?> clazz = Class.forName(name, false, parent);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        } catch (final ClassNotFoundException ignored) {
            // no-op
        }
        return null;
    }

    public Class<?> loadInternal(final String name, final boolean resolve) {
        try {
            final Class<?> clazz = findClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        } catch (final ClassNotFoundException ignored) {
            // no-op
        }
        return null;
    }
}
