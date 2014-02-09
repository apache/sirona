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

import org.apache.sirona.Role;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;

import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.ErrorData;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.tagext.BodyContent;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

public class SironaPageContext extends PageContext {
    private final PageContext delegate;
    private final AbstractPerformanceInterceptor.Context monitor;
    private boolean done = false;

    public SironaPageContext(final PageContext pageContext) {
        delegate = pageContext;
        monitor = new JspInterceptor().before(this, extractJspName());
    }

    @Override
    public void initialize(final Servlet servlet, final ServletRequest request, final ServletResponse response,
                           final String errorPageURL, final boolean needsSession,
                           final int bufferSize, final boolean autoFlush) throws IOException, IllegalStateException, IllegalArgumentException {
        delegate.initialize(servlet, request, response, errorPageURL, needsSession, bufferSize, autoFlush);
    }

    @Override
    public void release() {
        delegate.release();
        if (!done) {
            monitor.stop();
            done = true;
        }
    }

    @Override
    public HttpSession getSession() {
        return delegate.getSession();
    }

    @Override
    public Object getPage() {
        return delegate.getPage();
    }

    @Override
    public ServletRequest getRequest() {
        return delegate.getRequest();
    }

    @Override
    public ServletResponse getResponse() {
        return delegate.getResponse();
    }

    @Override
    public Exception getException() {
        return delegate.getException();
    }

    @Override
    public ServletConfig getServletConfig() {
        return delegate.getServletConfig();
    }

    @Override
    public ServletContext getServletContext() {
        return delegate.getServletContext();
    }

    @Override
    public void forward(final String relativeUrlPath) throws ServletException, IOException {
        delegate.forward(relativeUrlPath);
    }

    @Override
    public void include(final String relativeUrlPath) throws ServletException, IOException {
        delegate.include(relativeUrlPath);
    }

    @Override
    public void include(final String relativeUrlPath, final boolean flush) throws ServletException, IOException {
        delegate.include(relativeUrlPath, flush);
    }

    @Override
    public void handlePageException(final Exception e) throws ServletException, IOException {
        monitor.stopWithException(e);
        done = true;
        delegate.handlePageException(e);
    }

    @Override
    public void handlePageException(final Throwable t) throws ServletException, IOException {
        monitor.stopWithException(t);
        done = true;
        delegate.handlePageException(t);
    }

    @Override
    public BodyContent pushBody() {
        return delegate.pushBody();
    }

    @Override
    public ErrorData getErrorData() {
        return delegate.getErrorData();
    }

    @Override
    public void setAttribute(final String name, final Object value) {
        delegate.setAttribute(name, value);
    }

    @Override
    public void setAttribute(final String name, final Object value, final int scope) {
        delegate.setAttribute(name, value, scope);
    }

    @Override
    public Object getAttribute(final String name) {
        return delegate.getAttribute(name);
    }

    @Override
    public Object getAttribute(final String name, final int scope) {
        return delegate.getAttribute(name, scope);
    }

    @Override
    public Object findAttribute(final String name) {
        return delegate.findAttribute(name);
    }

    @Override
    public void removeAttribute(final String name) {
        delegate.removeAttribute(name);
    }

    @Override
    public void removeAttribute(final String name, final int scope) {
        delegate.removeAttribute(name, scope);
    }

    @Override
    public int getAttributesScope(final String name) {
        return delegate.getAttributesScope(name);
    }

    @Override
    public Enumeration<String> getAttributeNamesInScope(final int scope) {
        return delegate.getAttributeNamesInScope(scope);
    }

    @Override
    public JspWriter getOut() {
        return delegate.getOut();
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return delegate.getExpressionEvaluator();
    }

    @Override
    public ELContext getELContext() {
        return delegate.getELContext();
    }

    @Override
    public VariableResolver getVariableResolver() {
        return delegate.getVariableResolver();
    }

    @Override
    public JspWriter pushBody(final Writer writer) {
        return delegate.pushBody(writer);
    }

    @Override
    public JspWriter popBody() {
        return delegate.popBody();
    }

    protected String extractJspName() {
        final ServletRequest request = getRequest();
        if (HttpServletRequest.class.isInstance(request)) {
            return HttpServletRequest.class.cast(request).getRequestURI();
        }
        return request.getServletContext().getContextPath();
    }

    private static class JspInterceptor extends AbstractPerformanceInterceptor<SironaPageContext> {
        @Override
        protected Object proceed(final SironaPageContext invocation) throws Throwable {
            throw new UnsupportedOperationException("shouldn't be called");
        }

        @Override
        protected String getCounterName(final SironaPageContext invocation) {
            return invocation.extractJspName();
        }

        @Override
        protected Role getRole() {
            return Role.JSP;
        }

        @Override
        protected Context before(final SironaPageContext invocation, final String name) {
            return super.before(invocation, name);
        }
    }
}
