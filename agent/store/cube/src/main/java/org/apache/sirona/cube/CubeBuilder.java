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
package org.apache.sirona.cube;

import org.apache.sirona.SironaException;
import org.apache.sirona.configuration.ioc.AutoSet;
import org.apache.sirona.util.Localhosts;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.bind.DatatypeConverter;
import java.io.FileInputStream;
import java.security.KeyStore;

@AutoSet
public class CubeBuilder {
    private String proxyHost;
    private int proxyPort;
    private String collector;
    private String marker;
    private SSLSocketFactory socketFactory;

    // ssl config
    private String sslTrustStore;
    private String sslTrustStoreType;
    private String sslTrustStorePassword;
    private String sslTrustStoreProvider;
    private String sslKeyStore;
    private String sslKeyStoreType;
    private String sslKeyStorePassword;
    private String sslKeyStoreProvider;
    private String basicHeader; // user:pwd
    private boolean useCompression = false;

    public synchronized Cube build() {
        if (marker == null) {
            marker = Localhosts.get();
        }

        if (sslKeyStore != null || sslTrustStore != null) {
            final KeyManager[] keyManagers = createKeyManager();
            final TrustManager[] trustManagers = createTrustManager();
            try {
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(keyManagers, trustManagers, new java.security.SecureRandom());
                socketFactory = sslContext.getSocketFactory();
            } catch (final Exception e) {
                throw new SironaException(e);
            }
        } else {
            socketFactory = null;
        }

        if (basicHeader != null) { // compute it
            basicHeader = "Basic " + DatatypeConverter.printBase64Binary(basicHeader.getBytes());
        }

        return new Cube(this);
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getCollector() {
        return collector;
    }

    public String getMarker() {
        return marker;
    }

    public SSLSocketFactory getSocketFactory() {
        return socketFactory;
    }

    public String getBasicHeader() {
        return basicHeader;
    }

    public boolean isUseCompression()
    {
        return useCompression;
    }

    public void setUseCompression( boolean useCompression )
    {
        this.useCompression = useCompression;
    }

    private TrustManager[] createTrustManager() {
        if (sslTrustStore == null) {
            return null;
        }

        try {
            KeyStore ks = KeyStore.getInstance(null == sslTrustStoreType ? KeyStore.getDefaultType() : sslTrustStoreType);
            char[] pwd;
            if (sslTrustStorePassword != null) {
                pwd = sslTrustStorePassword.toCharArray();
            } else {
                pwd = "changeit".toCharArray();
            }
            FileInputStream fis = new FileInputStream(sslTrustStore);
            try {
                ks.load(fis, pwd);
            } finally {
                fis.close();
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(null == sslTrustStoreProvider ? TrustManagerFactory.getDefaultAlgorithm() : sslTrustStoreProvider);
            tmf.init(ks);
            return tmf.getTrustManagers();
        } catch (final Exception e) {
            throw new SironaException(e);
        }
    }

    private KeyManager[] createKeyManager() {
        if (sslKeyStore == null) {
            return null;
        }

        try {
            KeyStore ks = KeyStore.getInstance(null == sslKeyStoreType ? KeyStore.getDefaultType() : sslKeyStoreType);
            char[] pwd;
            if (sslKeyStorePassword != null) {
                pwd = sslKeyStorePassword.toCharArray();
            } else {
                pwd = "changeit".toCharArray();
            }
            FileInputStream fis = new FileInputStream(sslKeyStore);
            try {
                ks.load(fis, pwd);
            } finally {
                fis.close();
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(null == sslKeyStoreProvider ? KeyManagerFactory.getDefaultAlgorithm() : sslKeyStoreProvider);
            kmf.init(ks, pwd);
            return kmf.getKeyManagers();
        } catch (final Exception e) {
            throw new SironaException(e);
        }
    }

    @Override
    public String toString() {
        return "CubeBuilder{" + collector + '}';
    }
}
