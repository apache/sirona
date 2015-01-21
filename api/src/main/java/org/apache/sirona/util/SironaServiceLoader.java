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
package org.apache.sirona.util;

import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * This class is a wrapper on the top of ServiceLoader (reverting on internal impl if 1.6 not available)
 *
 * @since 0.3
 */
public class SironaServiceLoader<S>
    implements Iterable<S>
{

    private Class<S> service;

    private ClassLoader loader;

    private SironaServiceLoader( Class<S> service, ClassLoader loader )
    {
        this.service = service;
        this.loader = loader;
    }

    public static <S> SironaServiceLoader<S> load( Class<S> service, ClassLoader loader )
    {
        return new SironaServiceLoader<S>( service, loader );
    }

    @IgnoreJRERequirement
    public Iterator<S> iterator()
    {
        try
        {
            return java.util.ServiceLoader.load( this.service, this.loader ).iterator();
        }
        catch ( Throwable t )
        {
            // olamy revert to 1.5 way...
            return iterator1_5();
        }
    }

    /**
     * this method mimic the 1.6 ServiceLoader if you don't need 1.5 do not use that :-)
     *
     * @return
     */
    public Iterator<S> iterator1_5()
    {

        String resourceName = "META-INF/services/" + this.service.getName();

        try
        {
            List<String> serviceNames = new ArrayList<String>();

            Enumeration<URL> urls = this.loader.getResources( resourceName );
            while ( urls.hasMoreElements() )
            {
                serviceNames.addAll( parseFile( urls.nextElement() ) );

            }
            return initInstances( serviceNames ).iterator();
        }
        catch ( Throwable t )
        {
            throw new RuntimeException( t.getMessage(), t );
        }
    }

    private List<String> parseFile( URL url )
        throws IOException
    {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        List<String> classNames = new ArrayList<String>();
        try
        {
            inputStream = url.openStream();
            // we presume it's utf-8!!
            bufferedReader = new BufferedReader( new InputStreamReader( inputStream, "utf-8" ) );
            String line = bufferedReader.readLine();
            while ( line != null )
            {
                line = line.trim();
                // we ignore line starting with comments or empty
                if ( !line.startsWith( "#" ) && line.length() > 0 )
                {
                    classNames.add( line );
                }

                line = bufferedReader.readLine();
            }
            return classNames;
        }
        finally
        {
            if ( inputStream != null )
            {
                inputStream.close();
            }
            if ( bufferedReader != null )
            {
                bufferedReader.close();
            }
        }
    }

    private List<S> initInstances( List<String> classNames )
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {

        List<S> instances = new ArrayList<S>( classNames.size() );

        for ( String className : classNames )
        {
            Class<?> clazz = this.loader.loadClass( className );
            instances.add( (S) clazz.newInstance() );
        }

        return instances;
    }

}
