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

package org.apache.commons.monitoring.web.jsp;

import org.apache.commons.monitoring.counter.Role;
import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.stopwatches.StopWatch;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import static org.apache.commons.monitoring.web.jsp.TagUtils.getScope;

/**
 * A JSP tag to counter JSP rendering performances
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class StartTag extends TagSupport {
    private String id;
    private String scope;
    private String name;

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int doStartTag() throws JspException {
        final StopWatch stopWatch = Repository.INSTANCE.start(Repository.INSTANCE.getCounter(new Counter.Key(Role.JSP, name)));
        if (scope != null) {
            pageContext.setAttribute(id, stopWatch, getScope(scope));
        } else {
            pageContext.setAttribute(id, stopWatch);
        }
        return EVAL_PAGE;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setName(String name) {
        this.name = name;
    }
}
