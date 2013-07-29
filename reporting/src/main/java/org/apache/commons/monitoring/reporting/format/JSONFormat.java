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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.monitoring.monitors.Monitor;

import java.io.PrintWriter;


/**
 * Format to JSON (JavaScript), with optional indentation
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class JSONFormat implements Format {
    private boolean indent;

    public JSONFormat(boolean indent) {
        this.indent = indent;
    }

    public void repositoryStart(PrintWriter writer) {
        writer.append("{");
    }

    public void repositoryEnd(PrintWriter writer) {
        if (indent) {
            writer.append("\n");
        }
        writer.append("}");
    }

    public void monitorStart(PrintWriter writer, Monitor monitor) {
        if (indent) {
            writer.append("\n  ");
        }
        Monitor.Key key = monitor.getKey();
        escape(writer, key.getName());
        writer.append(":{");
        if (indent) {
            writer.append("\n    \"category\": \"").append(monitor.getKey().getCategory()).append("\",");
        }
    }

    public void monitorEnd(PrintWriter writer, String name) {
        if (indent) {
            writer.append("\n  ");
        }

        writer.append("}");
    }

    public void counterStart(PrintWriter writer, String name) {
        if (indent) {
            writer.append("\n    ");
        }
        escape(writer, name);
        writer.append(":{");
        writeAttribute(writer, "type", "counter");
    }

    public void counterEnd(PrintWriter writer, String name) {
        writer.append("}");
    }

    public void attribute(PrintWriter writer, String name, String value) {
        separator(writer);
        writeAttribute(writer, name, value);
    }

    protected void writeAttribute(PrintWriter writer, String name, String value) {
        writer.append("\"").append(name).append("\"").append(":");
        escape(writer, value);
    }

    public void escape(PrintWriter writer, String string) {
        writer.append('\"').append(StringEscapeUtils.escapeEcmaScript(string)).append('\"');
    }

    public void separator(PrintWriter writer) {
        writer.append(",");
    }
}