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
package org.apache.sirona.jpa;

import org.apache.sirona.SironaException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;

public class OverridePersistenceXmlClassLoader extends ClassLoader {
    private static final String PERSISTENCE_XML = "META-INF/persistence.xml";
    private static final String PERSISTENCE_PROVIDER = SironaPersistence.class.getName();
    public static final String NO_PROVIDER = "<provider></provider>";

    private final String replacement;

    public OverridePersistenceXmlClassLoader(final ClassLoader parent, final String replacement) {
        super(parent);
        this.replacement = replacement;
    }

    @Override // don't load anything from here
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }

    @Override
    public URL getResource(final String name) {
        final URL url = super.getResource(name);
        if (PERSISTENCE_XML.equals(name) && url != null) {
            return newUrl(url, slurp(url));
        }
        return  url;
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        final Enumeration<URL> urls = super.getResources(name);
        if (PERSISTENCE_XML.equals(name)) {
            final Collection<URL> overrided = new LinkedList<URL>();
            while (urls.hasMoreElements()) {
                final URL url = urls.nextElement();
                overrided.add(newUrl(url, slurp(url)));
            }
            return Collections.enumeration(overrided);
        }
        return  urls;
    }

    private URL newUrl(final URL url, final String slurp) {
        if (slurp.contains(PERSISTENCE_PROVIDER)) {
            final String afterReplace = slurp.replace(PERSISTENCE_PROVIDER, replacement).replace(NO_PROVIDER, "");
            try {
                return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), new ConstantURLStreamHandler(afterReplace));
            } catch (final MalformedURLException e) {
                // no-op
            }
        }
        return url;
    }

    private static String slurp(final URL url) {
        InputStream is = null;
        try {
            is = url.openStream();
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            return new String(out.toByteArray());
        } catch (final IOException e) {
            throw new SironaException(e);
        } finally {
            // no need to close out
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // no-op
            }
        }
    }

    private static class ConstantURLStreamHandler extends URLStreamHandler {
        private final String value;

        private ConstantURLStreamHandler(final String value) {
            this.value = value;
        }

        @Override
        protected URLConnection openConnection(final URL u) throws IOException {
            return new ConstantURLConnection(u, value);
        }
    }

    private static class ConstantURLConnection extends URLConnection {
        private final String value;

        private ConstantURLConnection(final URL url, final String value) {
            super(url);
            this.value = value;
        }

        @Override
        public void connect() throws IOException {
            // no-op
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(value.getBytes());
        }
    }
}
