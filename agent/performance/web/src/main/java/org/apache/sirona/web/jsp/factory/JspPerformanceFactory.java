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
package org.apache.sirona.web.jsp.factory;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspEngineInfo;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

public class JspPerformanceFactory extends JspFactory {
    private final JspFactory delegate;

    public JspPerformanceFactory(final JspFactory defaultFactory) {
        delegate = defaultFactory;
    }

    @Override
    public PageContext getPageContext(final Servlet servlet, final ServletRequest servletRequest,
                                      final ServletResponse servletResponse, final String s,
                                      final boolean b, int i, final boolean b2) {
        final PageContext pageContext = delegate.getPageContext(servlet, servletRequest, servletResponse, s, b, i, b2);
        return new SironaPageContext(pageContext);
    }

    @Override
    public void releasePageContext(final PageContext pageContext) {
        delegate.releasePageContext(pageContext);
    }

    @Override
    public JspApplicationContext getJspApplicationContext(final ServletContext servletContext) {
        // open door to wrap expression factory/el stuff
        return delegate.getJspApplicationContext(servletContext);
    }

    @Override
    public JspEngineInfo getEngineInfo() {
        return delegate.getEngineInfo();
    }
}
