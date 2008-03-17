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

package org.apache.commons.monitoring.reporting;

import java.io.PrintWriter;
import java.util.HashMap;

/**
 * A context for rendering process. Allow appending data to output using
 * <code>print</code> methods and to store/retrieve contextual data.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
@SuppressWarnings( "serial" )
public class Context
    extends HashMap<String, Object>
{
    private PrintWriter writer;

    public Context( PrintWriter writer )
    {
        super();
        this.writer = writer;
    }

    public void print( String s )
    {
        writer.print( s );
    }

    public void println( String s )
    {
        writer.println( s );
    }

}
