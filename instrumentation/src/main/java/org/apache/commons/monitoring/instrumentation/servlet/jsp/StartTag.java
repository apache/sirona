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

package org.apache.commons.monitoring.instrumentation.servlet.jsp;

import static org.apache.commons.monitoring.instrumentation.servlet.jsp.TagUtils.getScope;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.StopWatch;

/**
 * A JSP tag to monitor JSP rendering performances
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class StartTag
    extends TagSupport
{
    private String id;

    private String scope;

    private String name;

    private String category;

    private String subsystem;

    protected String repository;


    /**
     * @param id the id to set
     */
    public void setId( String id )
    {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int doStartTag()
        throws JspException
    {
        Repository repo = TagUtils.getRepository( pageContext, repository );

        StopWatch stopWatch = repo.start( repo.getMonitor( name, category, subsystem ) );
        if (scope != null)
        {
            pageContext.setAttribute( id, stopWatch, getScope( scope ) );
        }
        else
        {
            pageContext.setAttribute( id, stopWatch );
        }
        return EVAL_PAGE;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope( String scope )
    {
        this.scope = scope;
    }

    /**
     * @param name the name to set
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * @param category the category to set
     */
    public void setCategory( String category )
    {
        this.category = category;
    }

    /**
     * @param subsystem the subsystem to set
     */
    public void setSubsystem( String subsystem )
    {
        this.subsystem = subsystem;
    }

    public void setRepository( String repository )
    {
        this.repository = repository;
    }
}
