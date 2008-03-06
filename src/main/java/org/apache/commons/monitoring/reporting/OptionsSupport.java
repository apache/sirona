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

import java.util.Locale;

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
    public boolean render( Object object )
    {
        return true;
    }

    public boolean render( StatValue value, String attribute )
    {
        return true;
    }

    public Locale getLocale()
    {
        return Locale.US;
    }

    public Unit unitFor( StatValue value )
    {
        return value.getUnit();
    }
}
