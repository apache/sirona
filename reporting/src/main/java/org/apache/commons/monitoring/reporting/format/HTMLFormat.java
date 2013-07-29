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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO: write a real front?
public class HTMLFormat implements Format {
    private static final Collection<String> ATTRIBUTES_ORDERED_LIST = buildMetricDataHeader();

    private static Collection<String> buildMetricDataHeader() {
        final Collection<String> list = new CopyOnWriteArrayList<String>();
        list.add("Monitor");
        list.add("Category");
        list.add("Role");
        for (final MetricData md : MetricData.values()) {
            list.add(md.name());
        }
        return list;
    }

    private final Map<String, String> attributes = new HashMap<String, String>();

    public void repositoryStart(final PrintWriter writer) {
        writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n" +
            "<html>\n" +
            "  <head>\n" +
            "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n" +
            "    \n" +
            "    <title>Monitoring HTML Report</title>\n" +
            "\n" +
            "    <link href=\"" + RenderingContext.getBase() + "/css/theme.blue.css\" rel=\"stylesheet\">\n" +
            "    <script type=\"text/javascript\" language=\"javascript\" src=\"" + RenderingContext.getBase() + "/js/jquery.min.js" +"\"></script>\n" +
            "    <script type=\"text/javascript\" language=\"javascript\" src=\"" + RenderingContext.getBase() + "/js/jquery.tablesorter.js" +"\"></script>\n" +
            "    <script type=\"text/javascript\" language=\"javascript\" src=\"" + RenderingContext.getBase() + "/js/jquery.tablesorter.widgets.js" +"\"></script>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <div id=\"container\">\n" +
            "      <h1>Report</h1>\n" +
            "\n" +
            "      <table class=\"sort-table\" id=\"report-table\" border=\"1\">\n" +
            "        <thead>\n");
        writeColumnNames(writer);
        writer.write("        </thead>\n        <tbody>\n");
    }

    private void writeColumnNames(PrintWriter writer) {
        for (final String n : ATTRIBUTES_ORDERED_LIST) {
            writer.write("<th>");
            writer.write(n);
            writer.write("</th>\n");
        }
    }

    public void repositoryEnd(final PrintWriter writer) {
        writer.write("      </table>\n" +
            "\n" +
            "    </div>" +
            "" +
            "    <script type=\"text/javascript\">\n" +
            "      $(function(){\n" +
            "        $(\"#report-table\").tablesorter({\n" +
            "    theme: 'blue',\n" +
            "    widthFixed : true,\n" +
            "    widgets: [\"zebra\", \"filter\"],\n" +
            "    widgetOptions : {\n" +
            "      filter_childRows : false,\n" +
            "      filter_columnFilters : true,\n" +
            "      filter_cssFilter : 'tablesorter-filter',\n" +
            "      filter_filteredRow   : 'filtered',\n" +
            "      filter_formatter : null,\n" +
            "      filter_functions : null,\n" +
            "      filter_hideFilters : false, // true, (see note in the options section above)\n" +
            "      filter_ignoreCase : true,\n" +
            "      filter_liveSearch : true,\n" +
            "      filter_reset : 'button.reset',\n" +
            "      filter_searchDelay : 300,\n" +
            "      filter_serversideFiltering: false,\n" +
            "      filter_startsWith : false,\n" +
            "      filter_useParsedData : false\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "  });\n" +
            "      });" +
            "    </script>" +
            "  </body>\n" +
            "</html>");
    }

    public void monitorStart(final PrintWriter writer, final Monitor monitor) {
        attributes.put("Monitor", StringEscapeUtils.escapeHtml4(monitor.getKey().getName()));
        attributes.put("Category", monitor.getKey().getCategory());
    }

    public void monitorEnd(final PrintWriter writer, final String name) {
    }

    public void counterStart(final PrintWriter writer, final String name) {
        attributes.put("Role", name);
    }

    public void counterEnd(final PrintWriter writer, final String name) {
        writer.write("<tr>\n");
        for (final String key : ATTRIBUTES_ORDERED_LIST) {
            writer.write("<td>");
            final String value = attributes.get(key);
            if (value != null) {
                writer.write(value);
            } else {
                writer.write("");
            }
            writer.write("</td>\n");
        }
        writer.write("</tr>\n");
    }

    public void attribute(final PrintWriter writer, String name, final String value) {
        attributes.put(name, value);
    }

    public void separator(final PrintWriter writer) {
        // no-op
    }

    public void escape(final PrintWriter writer, final String string) {
        //  no-op
    }
}
