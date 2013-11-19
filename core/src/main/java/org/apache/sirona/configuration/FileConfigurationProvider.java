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
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileConfigurationProvider implements ConfigurationProvider {
    private static final Logger LOGGER = Logger.getLogger(FileConfigurationProvider.class.getName());

    private final String name;

    public FileConfigurationProvider(final String name) {
        this.name = name;
    }

    @Override
    public int ordinal() {
        return 50;
    }

    @Override
    public Properties configuration() {
        final Properties properties = new Properties();
        final String filename = System.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "configuration." + name, name);
        if (new File(filename).exists()) {
            try {
                properties.load(new FileInputStream(filename));
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        } else {
            // use core classloader and not TCCL to avoid to use app loader to load config
            final InputStream stream = FileConfigurationProvider.class.getClassLoader().getResourceAsStream(filename);
            if (stream != null) {
                try {
                    properties.load(stream);
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        return properties;
    }
}
