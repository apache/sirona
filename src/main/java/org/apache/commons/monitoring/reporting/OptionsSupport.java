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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Unit;

/**
 * Support class to implement <code>Renderer.Option</code>
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class OptionsSupport
    implements Renderer.Options
{
    public boolean render( Monitor monitor )
    {
        return true;
    }

    public boolean render( String role, String attribute )
    {
        return true;
    }

    public boolean renderRole( String role )
    {
        return true;
    }


    public Unit unitFor( StatValue value )
    {
        return value.getUnit();
    }

    public NumberFormat getDecimalFormat()
    {
        // Force JS compatible format
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator( '.' );
        return new DecimalFormat( "0.00", symbols );
    }

    public NumberFormat getNumberFormat()
    {
        return new DecimalFormat( "0" );
    }

    public DateFormat getDateFormat()
    {
        return DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT );
    }

}
