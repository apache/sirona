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

import org.apache.sirona.util.SironaServiceLoader;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// Configuration holder
public final class Configuration
{
    private static final Logger LOGGER = Logger.getLogger( Configuration.class.getName() );

    public static final String CONFIG_PROPERTY_PREFIX = "org.apache.sirona.";

    private static final String[] DEFAULT_CONFIGURATION_FILES =
        new String[]{ "sirona.properties", "collector-sirona.properties" };

    private static final Properties PROPERTIES = new Properties();

    static
    {
        try
        {
            final List<ConfigurationProvider> providers = new LinkedList<ConfigurationProvider>();
            for ( final String source : DEFAULT_CONFIGURATION_FILES )
            {
                providers.add( new FileConfigurationProvider( source ) );
            }
            providers.add( new PropertiesConfigurationProvider( System.getProperties() ) );

            ClassLoader classLoader = Configuration.class.getClassLoader();
            if ( classLoader == null )
            { // ServiceLoader fallbacks to it normally but ensure it is portable
                classLoader = ClassLoader.getSystemClassLoader();
            }
            for ( final ConfigurationProvider provider : SironaServiceLoader.load( ConfigurationProvider.class, //
                                                                                   classLoader ) )
            {
                providers.add( provider );
            }
            Collections.sort( providers, Sorter.INSTANCE );

            for ( final ConfigurationProvider provider : providers )
            {
                PROPERTIES.putAll( provider.configuration() );
            }
        }
        catch ( final Exception e )
        {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
        }
    }

    public static boolean is( final String key, final boolean defaultValue )
    {
        return Boolean.parseBoolean( getProperty( key, Boolean.toString( defaultValue ) ) );
    }

    public static int getInteger( final String key, final int defaultValue )
    {
        return Integer.parseInt( getProperty( key, Integer.toString( defaultValue ) ) );
    }

    public static String getProperty( final String key, final String defaultValue )
    {
        final String property = PROPERTIES.getProperty( key, defaultValue );
        if ( property != null && property.startsWith( "${" ) && property.endsWith( "}" ) )
        {
            return getProperty( property.substring( "${".length(), property.length() - 1 ), defaultValue );
        }
        return property;
    }

    public static String[] getArray( final String key, final String[] defaultValue )
    {
        String property = PROPERTIES.getProperty( key );
        if ( property == null )
        {
            return defaultValue;
        }
        return property.split( ";" );
    }

    public static Properties properties() {
        return PROPERTIES;
    }

    private Configuration()
    {
        // no-op
    }

    private static class Sorter
        implements Comparator<ConfigurationProvider>
    {
        public static final Comparator<? super ConfigurationProvider> INSTANCE = new Sorter();

        @Override
        public int compare( final ConfigurationProvider o1, final ConfigurationProvider o2 )
        {
            return o1.ordinal() - o2.ordinal();
        }
    }
}
