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

import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.internal.TextListener;
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
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                for (final FrameworkMethod mtd : getChildren()) {
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

    private static String[] buildProcessArgs(final FrameworkMethod mtd) throws IOException {
        final Collection<String> args = new ArrayList<String>();

        args.add( findJava() );

        AgentArgs agentArgs = mtd.getAnnotation( AgentArgs.class );

        String maxMem = agentArgs == null ? "" : agentArgs.maxMem();

        if ( maxMem.length() > 1 )
        {
            args.add( "-Xmx" + maxMem );
        }

        String minMem = agentArgs == null ? "" : agentArgs.minMem();

        if ( minMem.length() > 1 )
        {
            args.add( "-Xms" + minMem );
        }

        String javaAgentArgs =
            agentArgs == null ? null : StrSubstitutor.replace( agentArgs.value(), System.getProperties() );
        args.add( "-javaagent:" + buildJavaagent() + "=" + ( javaAgentArgs == null ? "" : javaAgentArgs ) );

        if ( Boolean.getBoolean( "test.debug.remote" ) )
        {
            args.add( "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=" + Integer.getInteger(
                "test.debug.remote.port", 8080 ) );
        }
        if ( agentArgs != null && agentArgs.noVerify() )
        {
            args.add( "-noverify" );
        }
        args.add( "-cp" );
        args.add(removeAgentFromCp(System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"))));
        args.add(JavaAgentRunner.class.getName());
        args.add(mtd.getMethod().getDeclaringClass().getName());
        args.add(mtd.getName());

        //  System.out.println("Running " + args.toString().replace(",", "").substring(1).replace("]", ""));

        return args.toArray(new String[args.size()]);
    }

    private static String removeAgentFromCp(final String property) {
        final String path = "target" + File.separator + "classes";
        final String sep = System.getProperty("path.separator");
        final String[] segments = property.split(sep);
        final StringBuilder builder = new StringBuilder(property.length());
        for (final String segment : segments) {
            if (!segment.endsWith(path)) {
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

    private static String buildJavaagent() throws IOException {
        return new File("target").listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith( System.getProperty( "javaagent.jar.name.start", "sirona-javaagent-" )) //
                        && name.endsWith(".jar") //
                        && !name.contains("sources");
                }
            })[0].getAbsolutePath();
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
}
