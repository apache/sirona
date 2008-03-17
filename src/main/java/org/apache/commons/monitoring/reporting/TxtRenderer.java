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

import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Monitor.Key;

/**
 * A simple TXT renderer, typically to dump monitoring status in a log file
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class TxtRenderer
    extends AbstractRenderer
{
    private static final String HR = "--------------------------------------------------------------------------------";

    /**
     * {@inheritDoc}
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(Context, java.lang.String)
     */
    @Override
    public void render( Context ctx, StatValue value, Options options )
    {
        ctx.println( value.getRole() );
        super.render( ctx, value, options );
        ctx.println( "" );
    }

    @Override
    protected void render( Context ctx, StatValue value, String attribute, Number number, Options options, int ratio )
    {
        ctx.print( "    " );
        ctx.print( attribute );
        ctx.print( " : " );
        super.render( ctx, value, attribute, number, options, ratio );
        ctx.println( "" );
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.monitoring.reporting.AbstractRenderer#render(org.apache.commons.monitoring.Monitor.Key)
     */
    @Override
    public void render( Context ctx, Key key )
    {
        ctx.println( HR );
        ctx.println( key.toString() );
        ctx.println( HR );
    }

}
