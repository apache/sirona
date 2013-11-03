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
(function (Sirona, $, undefined) {
    var dayDuration = 24 * 3600 * 1000;

    Sirona.extractTimeFromPicker = function (picker) {
        return picker.data('datetimepicker').getLocalDate().getTime();
    };

    Sirona.updateGraph = function (mapping, plugin, graph, start, end, options, complete, timeout) {
        $.ajax({
            url: mapping + "/" + plugin + "/" + graph + "/" + start + "/" + end,
            type: "GET",
            dataType: "json",
            success: function (data) {
                $.plot("#" + graph + "-graph", data, options);
            },
            //complete: complete, // TODO: find a better way to refresh the plot
            timeout: timeout
        });
    };

    Sirona.initGraph = function(mapping, plugin, graph, options) {
        var yesterday = new Date();
        yesterday.setTime(yesterday.getTime() - dayDuration);

        var tomorrow = new Date();
        tomorrow.setTime(tomorrow.getTime() + dayDuration);

        $('#' + graph + '-datetimepicker-start').data('datetimepicker').setLocalDate(yesterday);
        $('#' + graph + '-datetimepicker-end').data('datetimepicker').setLocalDate(tomorrow);

        (function doUpdateGraph() {
            Sirona.updateGraph(mapping, plugin, graph,
                Sirona.extractTimeFromPicker($('#' + graph + '-datetimepicker-start')),
                Sirona.extractTimeFromPicker($('#' + graph + '-datetimepicker-end')),
                options, doUpdateGraph, 4000); // refresh interval = 4s
        })();
    };
}(window.Sirona = window.Sirona || {}, jQuery));
