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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.sirona.repositories.Repository;
import org.junit.Ignore;
import org.junit.internal.TextListener;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

// only works with standard runner and surefire
public class JavaAgentRunner extends BlockJUnit4ClassRunner {
    public JavaAgentRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    // internal call to execute a single test
    public static void main(final String[] args) throws Exception {
        final Class<?> testClass = Class.forName(args[0]);

        final BlockJUnit4ClassRunner filteredRunner = new BlockJUnit4ClassRunner(testClass) {
            @Override
            protected List<FrameworkMethod> getChildren() {
                try {
                    return Arrays.asList(new FrameworkMethod(testClass.getMethod(args[1])));
                } catch (final NoSuchMethodException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(new TextListener(new PrintStream(baos)));
        final Result result = jUnitCore.run(filteredRunner);

        if (result.wasSuccessful()) {
            System.exit(0);
        }
        System.err.println(new String(baos.toByteArray()));
        System.exit(-1);
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                for (final FrameworkMethod mtd : getChildren()) {
                    if (mtd.getAnnotation(Ignore.class) != null) {
                        notifier.fireTestIgnored(describeChild(mtd));
                        continue;
                    }

                    final Description description = describeChild(mtd);
                    notifier.fireTestRunStarted(description);
                    try {
                        executeMethod(mtd, description, notifier);
                    } catch (final Exception e) {
                        notifier.fireTestFailure(new Failure(description, e));
                    } finally {
                        notifier.fireTestFinished(description);
                    }
                }
            }
        };

        final List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(BeforeFork.class);
        statement = befores.isEmpty() ? statement : new RunBefores(statement, befores, null);
        final List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(AfterFork.class);
        statement = befores.isEmpty() ? statement : new RunAfters(statement, afters, null);
        return statement;
    }

    private void executeMethod(final FrameworkMethod mtd, final Description description, final RunNotifier notifier) throws IOException, InterruptedException {
        final Process process = Runtime.getRuntime().exec(buildProcessArgs(mtd));

        slurp(process.getInputStream()).await();
        slurp(process.getErrorStream()).await();

        Runtime.getRuntime().addShutdownHook(new Thread() { // ctrl+x during the build
            @Override
            public void run() {
                try {
                    process.exitValue();
                } catch (final IllegalStateException ise) {
                    process.destroy();
                }
            }
        });

        process.waitFor();

        if (process.exitValue() != 0) {
            notifier.fireTestFailure(new Failure(description, new RuntimeException("exit code = " + process.exitValue())));
        }
    }

    protected String[] buildProcessArgs(final FrameworkMethod mtd) throws IOException {
        final Collection<String> args = new ArrayList<String>();

        args.add(findJava());

        AgentArgs agentArgs = mtd.getAnnotation(AgentArgs.class);

        String maxMem = agentArgs == null ? "" : agentArgs.maxMem();

        if (maxMem.length() > 0) {
            args.add("-Xmx" + maxMem);
        }

        String minMem = agentArgs == null ? "" : agentArgs.minMem();

        if (minMem.length() > 0) {
            args.add("-Xms" + minMem);
        }

        String javaAgentArgs =
                agentArgs == null || agentArgs.value().isEmpty() ? null : StrSubstitutor.replace(agentArgs.value(), System.getProperties());
        args.add("-javaagent:" + buildJavaagent() + "=" + (javaAgentArgs == null ? "" : javaAgentArgs));

        if (Boolean.getBoolean("test.debug.remote")) {
            args.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=" + Integer.getInteger(
                    "test.debug.remote.port", 5005));
        }
        if (agentArgs != null && agentArgs.noVerify()) {
            args.add("-noverify");
        }

        String[] vmArgs = agentArgs == null ? new String[0] : agentArgs.vmArgs();

        // java launcher not happy with empty arg....
        if (vmArgs.length>0 && !( vmArgs.length==1 && vmArgs[0].length()<1))
        {
            args.addAll( Arrays.asList( vmArgs ) );
        }


        String sysProps = agentArgs == null ? "" : agentArgs.sysProps();

        if (sysProps.length() > 0) {
            String[] splittedProps = StringUtils.split(sysProps, "|");
            for (String props : splittedProps) {
                String[] prop = StringUtils.split(props, "=");
                String key = prop[0];
                String value = "";
                if (prop.length > 1) {
                    value = prop[1].replace("${project.build.directory}", new File("target").getAbsolutePath());
                }
                args.add("-D" + key + "=" + StrSubstitutor.replace(value, System.getProperties()));
            }
        }

        String cp = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        if (agentArgs == null || agentArgs.removeTargetClassesFromClasspath()) {
            cp = removeAgentFromCp(cp);
        }
        if (agentArgs != null && agentArgs.removeSironaFromClasspath()) {
            cp = removeSironaFromCp(cp);
        }
        args.add("-cp");
        args.add(cp);
        args.add(JavaAgentRunner.class.getName());
        args.add(mtd.getMethod().getDeclaringClass().getName());
        args.add(mtd.getName());

        System.out.println("Running " + args.toString().replace(", ", " ").substring(1).replace("]", ""));

        return args.toArray(new String[args.size()]);
    }

    private static String removeAgentFromCp(final String property) {
        final String path = new File("target" + File.separatorChar + "classes").getAbsolutePath();
        return removeFromCp(property, new Predicate() {
            @Override
            public boolean accept(final String segment) {
                return !segment.equals(path);
            }
        });
    }

    private static String removeSironaFromCp(final String property) {
        return removeFromCp(property, new Predicate() {
            @Override
            public boolean accept(final String segment) {
                final String name = new File(segment).getName();
                // we remove sirona* but this jar with the runner
                return !name.startsWith("sirona-") || name.endsWith("-tests.jar");
            }
        });
    }

    private static String removeFromCp(final String property, final Predicate predicate) {
        final String sep = System.getProperty("path.separator");
        final String[] segments = property.split(sep);
        final StringBuilder builder = new StringBuilder(property.length());
        for (final String segment : segments) {
            if (predicate.accept(segment)) {
                builder.append(segment).append(sep);
            }
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    private static CountDownLatch slurp(final InputStream in) {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                int i;
                try {
                    while ((i = in.read()) != -1) {
                        System.out.write(i);
                    }
                    latch.countDown();
                } catch (final Exception e) {
                    latch.countDown();
                }
            }
        }.start();
        return latch;
    }

    protected String buildJavaagent() throws IOException {
        final String target = System.getProperty("javaagent.jar.directory", "target");
        if (target != null) {
            final File[] files = new File(target).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(System.getProperty("javaagent.jar.name.start", "sirona-javaagent-")) //
                            && name.endsWith(".jar") //
                            && name.endsWith("-shaded.jar");
                }
            });
            if (files != null && files.length > 0) {
                return files[0].getAbsolutePath();
            }
        }
        return JarLocation.get().getAbsolutePath();
    }

    private static String findJava() {
        {
            String home = System.getProperty("java.home");
            if (home != null) {
                return new File(home, "bin/java").getAbsolutePath();
            }
        }
        for (final String env : new String[]{"JAVA_HOME", "JRE_HOME"}) {
            final String home = System.getenv(env);
            if (home != null) {
                return new File(home, "bin/java").getAbsolutePath();
            }
        }
        return "java";
    }

    private static class JarLocation {

        public static File get() {
            return jarLocation(Repository.class);
        }

        public static File jarLocation(final Class clazz) {
            try {
                final String classFileName = clazz.getName().replace(".", "/") + ".class";

                final ClassLoader loader = clazz.getClassLoader();
                URL url;
                if (loader != null) {
                    url = loader.getResource(classFileName);
                } else {
                    url = clazz.getResource(classFileName);
                    if (url == null) {
                        url = clazz.getResource('/' + classFileName);
                    }
                }

                if (url == null) {
                    throw new IllegalStateException("classloader.getResource(classFileName) returned a null URL");
                }

                if ("jar".equals(url.getProtocol())) {
                    final String spec = url.getFile();

                    int separator = spec.indexOf('!');
                    if (separator == -1) {
                        throw new MalformedURLException("no ! found in jar url spec:" + spec);
                    }

                    url = new URL(spec.substring(0, separator));

                    return new File(decode(url.getFile()));

                } else if ("file".equals(url.getProtocol())) {
                    return toFile(classFileName, url);
                } else {
                    throw new IllegalArgumentException("Unsupported URL scheme: " + url.toExternalForm());
                }
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }

        public static File toFile(final String classFileName, final URL url) {
            String path = url.getFile();
            path = path.substring(0, path.length() - classFileName.length());
            return new File(decode(path));
        }


        public static String decode(final String fileName) {
            if (fileName.indexOf('%') == -1) return fileName;

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

                        final int d1 = Character.digit(fileName.charAt(i + 1), 16);
                        final int d2 = Character.digit(fileName.charAt(i + 2), 16);

                        if (d1 == -1 || d2 == -1) {
                            throw new IllegalArgumentException("Invalid % sequence (" + fileName.substring(i, i + 3) + ") at: " + String.valueOf(i));
                        }

                        out.write((byte) ((d1 << 4) + d2));

                        i += 3;

                    } while (i < fileName.length() && fileName.charAt(i) == '%');


                    result.append(out.toString());

                    continue;
                } else {
                    result.append(c);
                }

                i++;
            }
            return result.toString();
        }

    }

    private static interface Predicate {
        boolean accept(String path);
    }
}
