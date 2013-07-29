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
package org.apache.commons.monitoring.configuration;

import org.apache.commons.monitoring.MonitoringException;
import org.apache.commons.monitoring.util.ClassLoaders;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Configuration {
    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    private static final Collection<Closeable> INSTANCES = new ArrayList<Closeable>();

    public static final String COMMONS_MONITORING_PREFIX = "org.apache.commons.monitoring.";
    private static final String DEFAULT_CONFIGURATION_FILE = "commons-monitoring.properties";

    public static enum Keys {
        JMX("org.apache.commons.monitoring.jmx");

        public final String key;

        Keys(final String s) {
            key = s;
        }
    }

    private static final Properties PROPERTIES = new Properties(System.getProperties());
    static {
        try {
            final InputStream is = findConfiguration();
            if (is != null) {
                PROPERTIES.load(is);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    private static InputStream findConfiguration() throws FileNotFoundException {
        final String filename = System.getProperty(COMMONS_MONITORING_PREFIX + "configuration", DEFAULT_CONFIGURATION_FILE);
        if (new File(filename).exists()) {
            return new FileInputStream(filename);
        }

        return ClassLoaders.current().getResourceAsStream(filename);
    }

    public static <T> T newInstance(final Class<T> clazz) {
        try {
            String config = PROPERTIES.getProperty(clazz.getName());
            if (config == null) {
                config = clazz.getPackage().getName() + ".Default" + clazz.getSimpleName();
            }

            Class<?> loadedClass;
            try {
                loadedClass = ClassLoaders.current().loadClass(config);
            } catch (final Throwable throwable) { // NoClassDefFoundError or Exception
                loadedClass = clazz;
            }

            final Object instance = loadedClass.newInstance();
            if (Closeable.class.isInstance(instance)) {
                INSTANCES.add(Closeable.class.cast(instance));
            }
            return clazz.cast(instance);
        } catch (final Exception e) {
            throw new MonitoringException(e);
        }
    }

    public static boolean isActivated(final Keys key) {
        return Boolean.parseBoolean(getProperty(key.key, "false"));
    }

    public static String getProperty(final String key, final String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static void shutdown() {
        for (final Closeable c : INSTANCES) {
            try {
                c.close();
            } catch (IOException e) {
                // no-op
            }
        }
        INSTANCES.clear();
    }

    private Configuration() {
        // no-op
    }
}
