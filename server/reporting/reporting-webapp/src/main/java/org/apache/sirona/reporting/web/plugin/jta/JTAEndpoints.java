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
package org.apache.sirona.reporting.web.plugin.jta;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.reporting.web.plugin.api.Regex;
import org.apache.sirona.reporting.web.plugin.api.Template;

import static org.apache.sirona.reporting.web.plugin.api.graph.Graphs.generateReport;

public class JTAEndpoints {
    // copied to avoid classloading issue depending on the deployment, see org.apache.sirona.jta.JTAGauges
    private static final Role COMMITED = new Role("jta-commited", Unit.UNARY);
    private static final Role ROLLBACKED = new Role("jta-rollbacked", Unit.UNARY);
    private static final Role ACTIVE = new Role("jta-active", Unit.UNARY);

    @Regex
    public Template home() {
        return new Template("jta/jta.vm");
    }

    @Regex("/Commits/([0-9]*)/([0-9]*)")
    public String commit(final long start, final long end) {
        return generateReport("Commits", COMMITED, start, end);
    }

    @Regex("/Rollbacks/([0-9]*)/([0-9]*)")
    public String rollback(final long start, final long end) {
        return generateReport("Rollbacks", ROLLBACKED, start, end);
    }

    @Regex("/Actives/([0-9]*)/([0-9]*)")
    public String active(final long start, final long end) {
        return generateReport("Actives", ACTIVE, start, end);
    }
}
