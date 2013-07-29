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

package org.apache.commons.monitoring.reporting.format;

import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.repositories.Repository;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class FormattingVisitor extends AbstractVisitor {
    protected final PrintWriter writer;
    private final NumberFormat numberFormat;
    private final Format format;

    public FormattingVisitor(final Format format, final PrintWriter writer) {
        this(format, writer, RoleFilter.Defaults.PERFORMANCES);
    }

    public FormattingVisitor(final Format format, final PrintWriter writer, final RoleFilter filter) {
        super(filter);
        this.format = format;
        this.writer = writer;
        this.numberFormat = DecimalFormat.getNumberInstance(Locale.US);
        this.numberFormat.setMinimumFractionDigits(1);
    }

    protected void doVisit(final Repository repository) {
        boolean first = true;
        for (final Monitor monitor : getMonitors(repository)) {
            if (!first) {
                separator();
            }
            first = false;
            monitor.accept(this);
        }
    }

    protected void doVisit(final Monitor monitor) {
        boolean first = true;
        for (final Counter counter : getMetrics(monitor)) {
            if (!first) {
                separator();
            }
            first = false;
            counter.accept(this);
        }
    }

    protected void attribute(final String name, final double value) {
        attribute(name, format(value));
    }

    protected String format(final double value) {
        String s = numberFormat.format(value);
        if ("\uFFFD".equals(s)) {
            // Locale may have no DecimalFormatSymbols.NaN (set to "\uFFFD" (REPLACE_CHAR))
            s = "NaN";
        }
        return s;
    }

    @Override
    protected void repositoryStart() {
        format.repositoryStart(writer);
    }

    @Override
    protected void repositoryEnd() {
        format.repositoryEnd(writer);
    }

    protected void attribute(String name, String value) {
        format.attribute(writer, name, value);
    }

    @Override
    protected void counterEnd(String name) {
        format.counterEnd(writer, name);
    }

    @Override
    protected void counterStart(String name) {
        format.counterStart(writer, name);
    }

    protected void escape(String string) {
        format.escape(writer, string);
    }

    @Override
    protected void monitorStart(Monitor monitor) {
        format.monitorStart(writer, monitor);
    }

    @Override
    protected void monitorEnd(String name) {
        format.monitorEnd(writer, name);
    }

    protected void separator() {
        format.separator(writer);
    }


}