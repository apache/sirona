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

import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import static java.util.Arrays.asList;

public class SironaAgent {

    private static final boolean FORCE_RELOAD = Boolean.getBoolean("sirona.javaagent.force.reload");

    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        agentmain(agentArgs, instrumentation);
    }

    // all is done by reflection cause we change classloader to be able to enhance JVM too
    @IgnoreJRERequirement
    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {

        // just to get information on weird issues :-)
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                System.out.println("uncaughtException for thread: " + thread.getName() + ", message:" + throwable.getMessage());
                throwable.printStackTrace();

            }
        });

        final ClassLoader loader = ClassLoader.getSystemClassLoader(); // TCCL works for sirona but not for libs
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

        final boolean debug = "true".equalsIgnoreCase(extractConfig(agentArgs, "debug="));
        final boolean skipTempLoader = "true".equalsIgnoreCase(extractConfig(agentArgs, "skipTempLoader="));
        final boolean autoEvictClassLoaders = "true".equalsIgnoreCase(extractConfig(agentArgs, "autoEvictClassLoaders="));
        final String tempClassLoaders = extractConfig(agentArgs, "tempClassLoaders=");
        final boolean envrtDebug = debug || "true".equalsIgnoreCase(extractConfig(agentArgs, "environment-debug="));
        final String dumpOnExit = extractConfig(agentArgs, "dumpOnExit=");
        if (dumpOnExit != null) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                {
                    setName("sirona-dump-on-exit");
                }

                @Override
                public void run() {
                    FileWriter writer = null;
                    try {
                        writer = new FileWriter(dumpOnExit);
                        writer.write("name;role;unit;average;min;max;sum;hits;max concurrency\n");
                        for (final Counter c : Repository.INSTANCE.counters()) {
                            writer.write(c.getKey().getName() + ";" + c.getKey().getRole().getName() + ";" + c.getKey().getRole().getUnit().getName()
                                    + ";" + c.getMean() + ";" + c.getMin() + ";" + c.getMax() + ";" + c.getSum()
                                    + ";" + c.getHits() + ";" + c.getMaxConcurrency() + "\n");
                        }
                    } catch (final IOException e) {
                        throw new IllegalStateException(e);
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (final IOException e) {
                                // no-op
                            }
                        }
                    }
                }
            });
        }

        final StringBuilder out = new StringBuilder();
        final String libs = extractConfig(agentArgs, "libs=");
        if (libs != null) {
            for (final String lib : libs.split(" *, *")) {
                final File root = new File(lib);
                if (root.isFile() && root.getName().endsWith(".jar")) {
                    addLib(instrumentation, out, root);
                } else if (root.isDirectory()) {
                    final File[] children = root.listFiles();
                    if (children != null) {
                        for (final File f : children) {
                            addLib(instrumentation, out, f);
                        }
                    }
                }
            }
        }

        try { // eager init of static blocks
            Class.forName("org.apache.sirona.configuration.Configuration", true, loader);
            Class.forName("org.apache.sirona.javaagent.AgentContext", true, loader);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // we can log/sysout only from here
        if (envrtDebug) {
            System.out.print(out.toString());
        }

        try {
            // setup agent parameters
            Class<?> clazz = Class.forName("org.apache.sirona.javaagent.AgentContext", true, loader);
            Method addAgentParameterMethod = clazz.getMethod("addAgentParameter", new Class[]{String.class, String.class});
            Map<String, String> agentParameters = extractParameters(agentArgs);
            for (Map.Entry<String, String> entry : agentParameters.entrySet()) {
                addAgentParameterMethod.invoke(null, new String[]{entry.getKey(), entry.getValue() == null ? "" : entry.getValue()});

            }
        } catch (final Exception e) {
            e.printStackTrace();
        }


        try {
            if (debug) {
                System.out.println("Sirona debugging activated, find instrumented classes in /tmp/sirona-dump/");
            }

            final SironaTransformer transformer = new SironaTransformer(debug, skipTempLoader, tempClassLoaders);
            if (autoEvictClassLoaders) {
                final String evictTimeoutStr = extractConfig(agentArgs, "classLoaderEvictionTimeout=");
                final long timeout = evictTimeoutStr != null && !evictTimeoutStr.isEmpty() ? Long.parseLong(evictTimeoutStr) : 60000;
                final Thread evictThread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(timeout);
                        } catch (final InterruptedException e) {
                            Thread.interrupted();
                            return;
                        }
                        transformer.evictClassLoaders();
                    }
                });
                evictThread.setName("sirona-classloader-cleanup");
                evictThread.setDaemon(true);
            }
            final boolean reloadable = instrumentation.isRetransformClassesSupported() && FORCE_RELOAD;
            instrumentation.addTransformer(transformer, reloadable);

            final Class<?> listener = loader.loadClass("org.apache.sirona.javaagent.spi.InvocationListener");

            if (envrtDebug) {
                System.out.println("ClassLoader: " + loader);
                System.out.println("Loading ClassLoader: " + listener.getClassLoader());
                if (URLClassLoader.class.isInstance(loader)) {
                    System.out.println("URLs: " + asList(URLClassLoader.class.cast(loader).getURLs()));
                }
            }

            if (reloadable) {
                for (final Class<?> clazz : instrumentation.getAllLoadedClasses()) {
                    if (!clazz.isArray()
                            && !listener.isAssignableFrom(clazz)
                            && instrumentation.isModifiableClass(clazz)) {
                        try {

                            debug(loader, "reload clazz: {0}", clazz.getName());

                            instrumentation.retransformClasses(clazz);
                        } catch (final Exception e) {
                            System.err.println("Can't instrument: " + clazz.getName() + "[" + e.getMessage() + "]");
                            if (isDebug(loader)) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                if (isDebug(loader)) {
                    System.out.println("do not reload classes");
                }
            }
        } catch (final Exception e) {
            if (isDebug(loader)) {
                System.out.println("finished instrumentation setup with exception:" + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    @IgnoreJRERequirement
    private static void addLib(final Instrumentation instrumentation, final StringBuilder out, final File f) {
        if (out != null) {
            out.append("Added ").append(f.getAbsolutePath()) //
                    .append(" to (instrumentation) classpath") //
                    .append(System.getProperty("line.separator"));
        }
        try {
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(f));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void debug(ClassLoader loader, String msg, Object... objects) {
        try {
            Method method = loader //
                    .loadClass("org.apache.sirona.javaagent.logging.SironaAgentLogging") //
                    .getMethod("debug", String.class, Object.class);

            method.invoke(null, msg, objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isDebug(ClassLoader loader) {
        try {
            return Boolean.class.cast(
                    loader.loadClass("org.apache.sirona.javaagent.logging.SironaAgentLogging") //
                            .getField("AGENT_DEBUG") //
                            .get(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
        for (int i = 0; i < fileName.length(); ) {
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

    /**
     * @param agentArgs foo=bar|beer=palepale|etc...
     * @return parameters
     */
    protected static Map<String, String> extractParameters(String agentArgs) {
        if (agentArgs == null || agentArgs.length() < 1) {
            return Collections.emptyMap();
        }

        String[] separatorSplitted = agentArgs.split("\\|");

        Map<String, String> params = new HashMap<String, String>(separatorSplitted.length / 2);

        for (final String agentArg : separatorSplitted) {
            int idx = agentArg.indexOf('=');
            if (idx >= 0) {
                String key = agentArg.substring(0, idx);
                String value = agentArg.substring(idx + 1, agentArg.length());
                params.put(key, value);
            } else {
                params.put(agentArg, "");
            }
        }

        return params;
    }
}
