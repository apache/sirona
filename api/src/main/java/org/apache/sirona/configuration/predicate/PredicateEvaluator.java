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
package org.apache.sirona.configuration.predicate;

import org.apache.sirona.spi.SPI;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

public final class PredicateEvaluator {
    private static final String NOT = "!";
    private static final char SEPARATOR = ':';

    private final Map<String, Predicate> predicates = new HashMap<String, Predicate>();
    private final boolean truePredicate;
    private final boolean trueValue;

    public PredicateEvaluator(final String configuration, final String sep) {
        if (configuration != null && configuration.length()>0) {
            final PrefixPredicate prefixPredicate = new PrefixPredicate();
            final SuffixPredicate suffixPredicate = new SuffixPredicate();
            final RegexPredicate regexPredicate = new RegexPredicate();
            final ContainersPredicate containersPredicate = new ContainersPredicate();

            // defaults
            predicates.put(prefixPredicate.prefix(), prefixPredicate);
            predicates.put(suffixPredicate.prefix(), suffixPredicate);
            predicates.put(regexPredicate.prefix(), regexPredicate);
            predicates.put(containersPredicate.prefix(), containersPredicate);
            predicates.put(TruePredicate.INSTANCE.prefix(), TruePredicate.INSTANCE);
            predicates.put("boolean", TruePredicate.INSTANCE); // just an alias for true since we can set false now

            // SPI
            for (final Predicate predicate : SPI.INSTANCE.find(Predicate.class, PredicateEvaluator.class.getClassLoader())) {
                predicates.put(predicate.prefix(), predicate);
            }

            final String[] segments = configuration.split(sep);
            for (final String segment : segments) {
                final String trim = segment.trim();

                final String prefix;
                final int separator = trim.indexOf(SEPARATOR);
                if (separator > 0 && trim.length() > separator) {
                    prefix = trim.substring(0, separator);
                } else {
                    throw new IllegalArgumentException("Need to specify a prefix, available are:" + predicates.keySet());
                }

                final Predicate predicate = predicates.get(prefix);
                if (predicate == null) {
                    throw new IllegalArgumentException("Can't find prefix '" + prefix + "'");
                }

                if (predicate == TruePredicate.INSTANCE) {
                    predicates.clear(); // no need to keep it in mem since we'll always return true
                    trueValue = Boolean.parseBoolean(trim.substring(separator + 1));
                    truePredicate = true;
                    return;
                }

                final String value = trim.substring(separator + 1);
                if (!value.startsWith(NOT)) {
                    predicate.addConfiguration(value, true);
                } else {
                    predicate.addConfiguration(value.substring(1), false);
                }
            }
            trueValue = false;
            truePredicate = false;
        } else {
            trueValue = false;
            truePredicate = false;
        }

        // no need to keep it in mem
        predicates.remove(TruePredicate.INSTANCE.prefix());
        predicates.remove("boolean");
    }

    public boolean matches(final String value) {
        if (truePredicate) {
            return trueValue;
        }

        for (final Predicate predicate : predicates.values()) {
            if (predicate.matches(value)) {
                return true;
            }
        }
        return false;
    }

    // exclude only filter, just an optimized version of N prefixes
    // a lot of prefixes uses the same prefix
    // so chaining them using substring is really faster when intrumenting a lot of classes
    private static class ContainersPredicate implements Predicate {
        private final Collection<String> containers = new CopyOnWriteArraySet<String>();

        @Override
        public String prefix() {
            return "container";
        }

        @Override
        public boolean matches(final String value) {
            for (final String container : containers) {
                if ("tomee".equalsIgnoreCase(container) || "openejb".equalsIgnoreCase(container)) {
                    if (value.startsWith("org.")) {
                        final String org = value.substring("org.".length());
                        if (org.startsWith("apache.")) {
                            final String apache = org.substring("apache.".length());
                            if (isTomcat(apache)
                                    || apache.startsWith("tomee") || apache.startsWith("openejb")
                                    || apache.startsWith("xbean") || apache.startsWith("bval")
                                    || apache.startsWith("openjpa") || apache.startsWith("geronimo")
                                    || apache.startsWith("webbeans") || apache.startsWith("myfaces")
                                    || apache.startsWith("cxf") || apache.startsWith("neethi")
                                    || apache.startsWith("activemq") || apache.startsWith("commons")) {
                                return true;
                            }
                        } else if (org.startsWith("slf4j.") || org.startsWith("metatype") || org.startsWith("hsqldb") || org.startsWith("eclipse.jdt")) {
                            return true;
                        }
                    } else if (value.startsWith("serp")) {
                        return true;
                    }
                } else if ("tomcat".equalsIgnoreCase(container)) {
                    if (value.startsWith("org.")) {
                        final String org = value.substring("org.".length());
                        if (org.startsWith("apache.")) {
                            final String sub = value.substring("org.apache.".length());
                            if (isTomcat(sub)) {
                                return true;
                            }
                        } else if (org.startsWith("eclipse.jdt")) {
                            return true;
                        }
                    }
                } else if ("jvm".equalsIgnoreCase(container)) {
                    if (value.startsWith("java")
                            || value.startsWith("sun") || value.startsWith("com.sun")
                            || value.startsWith("jdk.")) {
                        return true;
                    }
                    if (value.startsWith("org.")) {
                        final String sub = value.substring("org.".length());
                        if (sub.startsWith("omg") || sub.startsWith("xml.sax.")
                                || sub.startsWith("ietf") || sub.startsWith("jcp")
                                || sub.startsWith("apache.xerces")) {
                            return true;
                        }
                    }
                    final int length = "org.apache.".length();
                    if (value.length() >= length) {
                        final String sub = value.substring("org.apache.".length());
                        if (sub.startsWith("xerces")) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private static boolean isTomcat(final String sub) {
            return sub.startsWith("juli.") || sub.startsWith("catalina.")
                    || sub.startsWith("tomcat.") || sub.startsWith("jasper.")
                    || sub.startsWith("coyote.") || sub.startsWith("naming.")
                    || sub.startsWith("el.");
        }

        @Override
        public void addConfiguration(final String value, final boolean negative) {
            containers.add(value);
        }
    }

    private static class TruePredicate implements Predicate {
        private static final TruePredicate INSTANCE = new TruePredicate();

        private TruePredicate() {
            // no-op
        }

        @Override
        public String prefix() {
            return "true";
        }

        @Override
        public boolean matches(final String value) {
            return true;
        }

        @Override
        public void addConfiguration(final String value, final boolean negative) {
            // no-op
        }
    }

    private static class SuffixPredicate implements Predicate {
        private final Map<String, Boolean> suffixes = new HashMap<String, Boolean>();

        @Override
        public String prefix() {
            return "suffix";
        }

        @Override
        public boolean matches(final String value) {
            for (final Map.Entry<String, Boolean> p : suffixes.entrySet()) {
                if (value.endsWith(p.getKey())) {
                    return p.getValue();
                }
            }
            return false;
        }

        @Override
        public void addConfiguration(final String value, final boolean negative) {
            suffixes.put(value, negative);
        }
    }

    private static class PrefixPredicate implements Predicate {
        private final Map<String, Boolean> prefixes = new HashMap<String, Boolean>();

        @Override
        public String prefix() {
            return "prefix";
        }

        @Override
        public boolean matches(final String value) {
            for (final Map.Entry<String, Boolean> p : prefixes.entrySet()) {
                if (value.startsWith(p.getKey())) {
                    return p.getValue();
                }
            }
            return false;
        }

        @Override
        public void addConfiguration(final String value, final boolean negative) {
            prefixes.put(value, negative);
        }
    }

    private static class RegexPredicate implements Predicate {
        private final Map<Pattern, Boolean> patterns = new HashMap<Pattern, Boolean>();

        @Override
        public String prefix() {
            return "regex";
        }

        @Override
        public boolean matches(final String value) {
            for (final Map.Entry<Pattern, Boolean> p : patterns.entrySet()) {
                if (p.getKey().matcher(value).matches()) {
                    return p.getValue();
                }
            }
            return false;
        }

        @Override
        public void addConfiguration(final String value, final boolean negative) {
            patterns.put(Pattern.compile(value), negative);
        }
    }
}
