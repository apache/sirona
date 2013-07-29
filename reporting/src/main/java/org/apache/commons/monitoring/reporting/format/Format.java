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

import org.apache.commons.monitoring.monitors.Monitor;

import java.io.PrintWriter;

public interface Format {

    void repositoryStart(PrintWriter writer);

    void repositoryEnd(PrintWriter writer);

    void monitorStart(PrintWriter writer, Monitor monitor);

    void monitorEnd(PrintWriter writer, String name);

    void counterStart(PrintWriter writer, String name);

    void counterEnd(PrintWriter writer, String name);

    void attribute(PrintWriter writer, String name, String value);

    void separator(PrintWriter writer);

    void escape(PrintWriter writer, String string);

    public static interface Defaults {
        static final Format JSON = new JSONFormat(false);
        static final Format JSON_PRETTY = new JSONFormat(true);
        static final Format XML = new XMLFormat(false);
        static final Format XML_PRETTY = new XMLFormat(true);
        static final Format CSV = new CSVFormat();
        static final Format HTML = new HTMLFormat();
    }
}
