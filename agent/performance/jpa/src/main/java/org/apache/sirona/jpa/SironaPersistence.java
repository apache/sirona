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

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Unit;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.apache.sirona.jpa.JPAProxyFactory.monitor;

public class SironaPersistence implements PersistenceProvider {
    public static final Role ROLE = new Role("jpa", Unit.Time.NANOSECOND);

    private static final String DELEGATE_PROVIDER_KEY = Configuration.CONFIG_PROPERTY_PREFIX + "jpa.provider";
    private static final String DEFAULT_PROVIDER = System.getProperty(DELEGATE_PROVIDER_KEY);
    private static final Class<?>[] PROXY_API = new Class<?>[]{EntityManagerFactory.class, Serializable.class};

    private static final String[] PROVIDERS = {
        "org.apache.openjpa.persistence.PersistenceProviderImpl",
        "org.hibernate.jpa.HibernatePersistenceProvider",
        "org.hibernate.ejb.HibernatePersistence",
        "org.eclipse.persistence.jpa.PersistenceProvider",
        "oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider",
        "oracle.toplink.essentials.PersistenceProvider",
        "me.prettyprint.hom.CassandraPersistenceProvider",
        "org.datanucleus.jpa.PersistenceProviderImpl",
        "com.orientechnologies.orient.core.db.object.jpa.OJPAPersistenceProvider",
        "com.orientechnologies.orient.object.jpa.OJPAPersistenceProvider",
        "com.spaceprogram.simplejpa.PersistenceProviderImpl"
    };

    private volatile PersistenceProvider delegate;

    @Override
    public EntityManagerFactory createEntityManagerFactory(final String unit, final Map map) {
        final PersistenceProvider persistenceProvider = findDelegate(map);
        final ClassLoader tccl = tccl();

        final ClassLoader hack = new OverridePersistenceXmlClassLoader(tccl, persistenceProvider.getClass().getName());
        Thread.currentThread().setContextClassLoader(hack);
        try {
            final EntityManagerFactory entityManagerFactory = persistenceProvider.createEntityManagerFactory(unit, map);
            if (entityManagerFactory == null) {
                return null;
            }
            return EntityManagerFactory.class.cast(
                monitor(PROXY_API, entityManagerFactory, ROLE, true));
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(final PersistenceUnitInfo info, final Map map) {
        final PersistenceProvider persistenceProvider = findDelegate(map);
        final EntityManagerFactory containerEntityManagerFactory = persistenceProvider.createContainerEntityManagerFactory(
            PersistenceUnitInfo.class.cast(Proxy.newProxyInstance(tccl(), new Class<?>[]{PersistenceUnitInfo.class}, new ProviderAwareHandler(persistenceProvider.getClass().getName(), info))),
            map);
        if (containerEntityManagerFactory == null) {
            return null;
        }
        return EntityManagerFactory.class.cast(
            monitor(PROXY_API, containerEntityManagerFactory, ROLE, true));
    }

    @Override
    public ProviderUtil getProviderUtil() { // we suppose it is loaded later than createXXXEMF so we'll get the delegate
        return loadOrGuessDelegate(null).getProviderUtil();
    }

    private PersistenceProvider findDelegate(final Map map) {
        if (map == null) {
            return loadOrGuessDelegate(null);
        }
        return loadOrGuessDelegate(String.class.cast(map.get(DELEGATE_PROVIDER_KEY)));
    }

    private PersistenceProvider loadOrGuessDelegate(final String name) {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    if (name == null) {
                        if (DEFAULT_PROVIDER != null) {
                            try {
                                delegate = newPersistence(DEFAULT_PROVIDER);
                            } catch (final Exception e) {
                                throw new IllegalStateException(new ClassNotFoundException("Can't instantiate '" + DEFAULT_PROVIDER + "'"));
                            }
                        } else {
                            final PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();
                            if (resolver != null) {
                                final List<PersistenceProvider> instances = resolver.getPersistenceProviders();
                                if (instances != null) {
                                    for (final PersistenceProvider p : instances) {
                                        if (!SironaPersistence.class.isInstance(p)) {
                                            delegate = p;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (delegate == null) {
                                for (final String provider : PROVIDERS) {
                                    try {
                                        delegate = newPersistence(provider);
                                        if (delegate != null) {
                                            break;
                                        }
                                    } catch (final Throwable th2) {
                                        // no-op
                                    }
                                }
                            }
                        }

                        if (delegate == null) {
                            throw new IllegalStateException(new ClassNotFoundException("Can't find a delegate"));
                        }
                    } else {
                        try {
                            delegate = newPersistence(name);
                        } catch (final Exception e) {
                            throw new IllegalStateException(new ClassNotFoundException("Can't instantiate '" + name + "'"));
                        }
                    }
                }
            }
        }
        if (name != null && !delegate.getClass().getName().equals(name)) {
            try {
                return newPersistence(name);
            } catch (final Exception e) {
                throw new IllegalStateException(new ClassNotFoundException("Can't instantiate '" + name + "'"));
            }
        }
        return delegate;
    }

    private static ClassLoader tccl() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static PersistenceProvider newPersistence(final String name) throws Exception {
        return PersistenceProvider.class.cast(tccl().loadClass(name).newInstance());
    }

    private static class ProviderAwareHandler implements InvocationHandler {
        private final String provider;
        private final PersistenceUnitInfo info;

        public ProviderAwareHandler(final String provider, final PersistenceUnitInfo info) {
            this.provider = provider;
            this.info = info;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if ("getPersistenceProviderClassName".equals(method.getName())) {
                return provider;
            }
            try {
                return method.invoke(info, args);
            } catch (final InvocationTargetException ite) {
                throw ite.getTargetException();
            }
        }
    }
}
