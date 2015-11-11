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
package org.apache.sirona.output;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.pathtracking.PathCallInformation;
import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.store.DataStoreFactory;
import org.apache.sirona.store.tracking.PathTrackingDataStore;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PathTrackingDumper {
    private final PathTrackingDataStore store;
    private final Formatter formatter;
    private final Output output;
    private final boolean skipZero;

    public PathTrackingDumper() {
        this(
            defaultStore(),
            new DefaultFormatter(),
            System.out,
            false);
    }

    public PathTrackingDumper(final PathTrackingDataStore dataStore,
                              final boolean skipZero) {
        this(dataStore, new DefaultFormatter(), System.out, skipZero);
    }

    public PathTrackingDumper(final PathTrackingDataStore dataStore,
                              final Formatter formatter,
                              final Output output,
                              final boolean skipZero) {
        this.store = dataStore;
        this.formatter = formatter;
        this.output = output;
        this.skipZero = skipZero;
    }

    public PathTrackingDumper(final Formatter formatter,
                              final Output output,
                              final boolean skipZero) {
        this(defaultStore(), formatter, output, skipZero);
    }

    public PathTrackingDumper(final PathTrackingDataStore dataStore,
                              final Formatter formatter,
                              final Logger output,
                              final boolean skipZero) {
        this(dataStore, formatter, new Output() {
            public void write(final String line) {
                output.info(line);
            }
        }, skipZero);
    }

    public PathTrackingDumper(final PathTrackingDataStore dataStore,
                              final Formatter formatter,
                              final PrintStream output,
                              final boolean skipZero) {
        this(dataStore, formatter, new Output() {
            public void write(final String line) {
                output.println(line);
            }
        }, skipZero);
    }

    public String dumpAsString(final Date from, final Date to) {
        final StringBuilder builder = new StringBuilder();
        dump(new Output() {
            public void write(final String line) {
                builder.append(line).append('\n'); // unix style, dont use line.separator to ensure we know the format to parse if needed
            }
        }, from, to);
        return builder.toString();
    }

    public void dump(final Date from, final Date to) {
        dump(output, from, to);
    }

    public void dump(final Output output, final Date from, final Date to) {
        final Collection<PathCallInformation> informations = store.retrieveTrackingIds(from, to);
        if (informations != null) {
            for (final PathCallInformation info : informations) {
                final List<PathTrackingEntry> retrieve = new ArrayList<PathTrackingEntry>(store.retrieve(info.getTrackingId()));
                Collections.sort(retrieve, new Comparator<PathTrackingEntry>() {
                    public int compare(final PathTrackingEntry o1, final PathTrackingEntry o2) {
                        final long l = o1.getStartTime() - o2.getStartTime();
                        if (l == 0) {
                            final int i = o1.getLevel() - o2.getLevel();
                            if (i == 0) { // here starts the no luck cases
                                final int name = o1.getClassName().compareTo(o2.getClassName());
                                if (name == 0) {
                                    return o1.getMethodName().compareTo(o2.getMethodName());
                                }
                                return name;
                            }
                            return i;
                        }
                        return l < 0 ? -1 : 1;
                    }
                });
                for (final PathTrackingEntry pathTrackingEntry : retrieve) {
                    final long duration = TimeUnit.NANOSECONDS.toMillis(pathTrackingEntry.getExecutionTime());
                    if (duration != 0 || !skipZero) {
                        output.write(formatter.format(pathTrackingEntry));
                    }
                }
            }
        }
    }

    private static PathTrackingDataStore defaultStore() {
        return IoCs.findOrCreateInstance(DataStoreFactory.class).getPathTrackingDataStore();
    }

    public interface Formatter {
        String format(PathTrackingEntry entry);
    }

    public static class DefaultFormatter implements Formatter {
        public String format(final PathTrackingEntry pathTrackingEntry) {
            final long duration = TimeUnit.NANOSECONDS.toMillis(pathTrackingEntry.getExecutionTime());
            return tabSpaces(pathTrackingEntry) +
                pathTrackingEntry.getLevel() + " " +
                pathTrackingEntry.getClassName() + "#" + pathTrackingEntry.getMethodName() +
                " -> " + duration + "ms";
        }

        protected static String tabSpaces(final PathTrackingEntry pathTrackingEntry) {
            final StringBuilder tabs = new StringBuilder();
            for (int i = 0; i < pathTrackingEntry.getLevel() - 1; i++) {
                tabs.append("  ");
            }
            return tabs.toString();
        }
    }
}
