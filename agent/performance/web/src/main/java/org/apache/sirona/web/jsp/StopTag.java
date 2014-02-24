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

package org.apache.sirona.web.jsp;

import org.apache.sirona.stopwatches.StopWatch;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import static org.apache.sirona.web.jsp.TagUtils.getScope;

/**
 * A JSP tag to counter JSP rendering performances
 *
 *
 */
public class StopTag extends TagSupport {
    private String id;
    private String scope;

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {
        StopWatch stopWatch;
        if (scope != null) {
            stopWatch = (StopWatch) pageContext.getAttribute(id, getScope(scope));
        } else {
            stopWatch = (StopWatch) pageContext.getAttribute(id);
        }
        if (stopWatch == null) {
            throw new JspException("No StopWatch under ID " + id + " and scope " + scope);
        }
        stopWatch.stop();
        return EVAL_PAGE;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(final String scope) {
        this.scope = scope;
    }
}
