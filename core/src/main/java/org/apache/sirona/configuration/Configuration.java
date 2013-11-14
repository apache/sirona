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
package org.apache.sirona.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// Configuration holder
public final class Configuration {
    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    public static final String CONFIG_PROPERTY_PREFIX = "org.apache.sirona.";

    private static final String SYS_PROPS_FILE_PATH = "sirona.properties";

    private static final String[] DEFAULT_CONFIGURATION_FILES = new String[]{ "sirona.properties", "collector-sirona.properties" };

    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            final InputStream is = findConfiguration();
            if (is != null) {
                PROPERTIES.load(is);
            }
            PROPERTIES.putAll( System.getProperties() );
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static InputStream findConfiguration() throws FileNotFoundException {

        String sysPropsPath = System.getProperty( SYS_PROPS_FILE_PATH );

        if (sysPropsPath != null){
            File file = new File( sysPropsPath );
            if (file.exists()){
                return new FileInputStream( file );
            } else {
                LOGGER.log(Level.WARNING, "sirona configuration file with path " + sysPropsPath + " cannot be found so ignore it");
            }
        }

        for (final String cf : DEFAULT_CONFIGURATION_FILES) {
            final String filename = System.getProperty(CONFIG_PROPERTY_PREFIX + "configuration", cf);
            if (new File(filename).exists()) {
                return new FileInputStream(filename);
            }

            // use core classloader and not TCCL to avoid to use app loader to load config
            final InputStream stream = Configuration.class.getClassLoader().getResourceAsStream(filename);
            if (stream != null) {
                return stream;
            }
        }
        return null;
    }

    public static boolean is(final String key, final boolean defaultValue) {
        return Boolean.parseBoolean(getProperty(key, Boolean.toString(defaultValue)));
    }

    public static int getInteger(final String key, final int defaultValue) {
        return Integer.parseInt(getProperty(key, Integer.toString(defaultValue)));
    }

    public static String getProperty(final String key, final String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

    private Configuration() {
        // no-op
    }
}
