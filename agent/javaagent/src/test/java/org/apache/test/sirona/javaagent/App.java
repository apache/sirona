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

package org.apache.test.sirona.javaagent;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class App
{

    public void foo()
        throws Exception
    {
        Thread.sleep( 500 );
    }

    public void beer()
        throws Exception
    {
        this.foo();
        this.pub( "blabla", Arrays.asList( "Mountain Goat", "Fatyak" ), 2 );
    }

    public void pub( String foo, List<String> beers, int i )
        throws Exception
    {
        Thread.sleep( 100 );
        this.bar();
    }

    public App bar()
        throws Exception
    {
        Thread.sleep( 300 );
        return this;
    }


    public App redirectStreamout()
    {
        System.setOut( new LogHandler( System.out ) );
        return this;
    }

    private static class LogHandler
        extends PrintStream
    {
        OutputStream outputStream;

        public LogHandler( OutputStream outputStream )
        {
            super( outputStream );
            this.outputStream = outputStream;
        }

        @Override
        public void println( String s )
        {
            try
            {
                super.write( s.getBytes() );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

}
