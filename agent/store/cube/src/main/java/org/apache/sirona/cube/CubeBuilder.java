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
import org.apache.sirona.configuration.ioc.Created;
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

    /**
     * compression off per default
     */
    private boolean useCompression = false;

    /**
     * default timeout of 5s
     */
    private int postTimeout = 5000;

    /**
     * only for httpclient
     */
    private int maxTotalConnections = 10;

    /**
     * only for httpclient
     */
    private int defaultMaxPerRoute = 10;

    /**
     * only for httpclient
     */
    private int connectionTimeout = 5000;

    /**
     * only for httpclient
     */
    private int connectionRequestTimeout = 5000;

    /**
     * only for httpclient
     */
    private boolean useAsync;

    private int asyncIoThreadCount = Runtime.getRuntime().availableProcessors() - 1;

    private Cube cubeInstance;

    @Created
    public void createInstance()
    {
        cubeInstance = this.build();
    }

    public synchronized Cube build() {
        if (cubeInstance != null)
        {
            return cubeInstance;
        }
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

    public int getPostTimeout()
    {
        return postTimeout;
    }

    public void setPostTimeout( int postTimeout )
    {
        this.postTimeout = postTimeout;
    }

    protected TrustManager[] createTrustManager() {
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

    protected KeyManager[] createKeyManager() {
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

    public String getSslTrustStore()
    {
        return sslTrustStore;
    }

    public String getSslTrustStoreType()
    {
        return sslTrustStoreType;
    }

    public String getSslTrustStorePassword()
    {
        return sslTrustStorePassword;
    }

    public String getSslTrustStoreProvider()
    {
        return sslTrustStoreProvider;
    }

    public String getSslKeyStore()
    {
        return sslKeyStore;
    }

    public String getSslKeyStoreType()
    {
        return sslKeyStoreType;
    }

    public String getSslKeyStorePassword()
    {
        return sslKeyStorePassword;
    }

    public String getSslKeyStoreProvider()
    {
        return sslKeyStoreProvider;
    }

    public int getMaxTotalConnections()
    {
        return maxTotalConnections;
    }

    public int getDefaultMaxPerRoute()
    {
        return defaultMaxPerRoute;
    }

    public int getConnectionTimeout()
    {
        return connectionTimeout;
    }

    public int getConnectionRequestTimeout()
    {
        return connectionRequestTimeout;
    }

    public void setProxyHost( String proxyHost )
    {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort( int proxyPort )
    {
        this.proxyPort = proxyPort;
    }

    public void setCollector( String collector )
    {
        this.collector = collector;
    }

    public void setMarker( String marker )
    {
        this.marker = marker;
    }

    public void setSocketFactory( SSLSocketFactory socketFactory )
    {
        this.socketFactory = socketFactory;
    }

    public void setSslTrustStore( String sslTrustStore )
    {
        this.sslTrustStore = sslTrustStore;
    }

    public void setSslTrustStoreType( String sslTrustStoreType )
    {
        this.sslTrustStoreType = sslTrustStoreType;
    }

    public void setSslTrustStorePassword( String sslTrustStorePassword )
    {
        this.sslTrustStorePassword = sslTrustStorePassword;
    }

    public void setSslTrustStoreProvider( String sslTrustStoreProvider )
    {
        this.sslTrustStoreProvider = sslTrustStoreProvider;
    }

    public void setSslKeyStore( String sslKeyStore )
    {
        this.sslKeyStore = sslKeyStore;
    }

    public void setSslKeyStoreType( String sslKeyStoreType )
    {
        this.sslKeyStoreType = sslKeyStoreType;
    }

    public void setSslKeyStorePassword( String sslKeyStorePassword )
    {
        this.sslKeyStorePassword = sslKeyStorePassword;
    }

    public void setSslKeyStoreProvider( String sslKeyStoreProvider )
    {
        this.sslKeyStoreProvider = sslKeyStoreProvider;
    }

    public void setBasicHeader( String basicHeader )
    {
        this.basicHeader = basicHeader;
    }

    public void setMaxTotalConnections( int maxTotalConnections )
    {
        this.maxTotalConnections = maxTotalConnections;
    }

    public void setDefaultMaxPerRoute( int defaultMaxPerRoute )
    {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
    }

    public void setConnectionTimeout( int connectionTimeout )
    {
        this.connectionTimeout = connectionTimeout;
    }

    public void setConnectionRequestTimeout( int connectionRequestTimeout )
    {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public boolean isUseAsync()
    {
        return useAsync;
    }

    public void setUseAsync( boolean useAsync )
    {
        this.useAsync = useAsync;
    }

    public int getAsyncIoThreadCount()
    {
        return asyncIoThreadCount;
    }

    public void setAsyncIoThreadCount( int asyncIoThreadCount )
    {
        this.asyncIoThreadCount = asyncIoThreadCount;
    }

    @Override
    public String toString() {
        return "CubeBuilder{" + collector + '}';
    }
}
