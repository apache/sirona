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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public final class PredicateEvaluator {
    private static final String NOT = "!";
    private static final char SEPARATOR = ':';

    private final Map<String, Predicate> predicates = new HashMap<String, Predicate>();
    private final boolean truePredicate;

    public PredicateEvaluator(final String configuration, final String sep) {
        if (configuration != null && !configuration.isEmpty()) {
            final PrefixPredicate prefixPredicate = new PrefixPredicate();
            final SuffixPredicate suffixPredicate = new SuffixPredicate();
            final RegexPredicate regexPredicate = new RegexPredicate();

            // defaults
            predicates.put(prefixPredicate.prefix(), prefixPredicate);
            predicates.put(suffixPredicate.prefix(), suffixPredicate);
            predicates.put(regexPredicate.prefix(), regexPredicate);
            predicates.put(TruePredicate.INSTANCE.prefix(), TruePredicate.INSTANCE);

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
                    truePredicate = true;
                    predicates.clear(); // no need to keep it in mem since we'll always return true
                    return;
                }

                final String value = trim.substring(separator + 1);
                if (!value.startsWith(NOT)) {
                    predicate.addConfiguration(value, true);
                } else {
                    predicate.addConfiguration(value.substring(1), false);
                }
            }
            truePredicate = false;
        } else {
            truePredicate = false;
        }
        predicates.remove(TruePredicate.INSTANCE.prefix()); // no need to keep it in mem
    }

    public boolean matches(final String value) {
        if (truePredicate) {
            return true;
        }

        for (final Predicate predicate : predicates.values()) {
            if (predicate.matches(value)) {
                return true;
            }
        }
        return false;
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
