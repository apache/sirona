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
package org.apache.sirona.reporting.web.status;

import org.apache.sirona.status.Status;

import java.util.HashMap;
import java.util.Map;

public final class StatusHelper
{
    private static final Map<Status, String> MAPPING = new HashMap<Status, String>( 3 );

    static
    {
        MAPPING.put( Status.OK, "success" );
        MAPPING.put( Status.DEGRADED, "warning" );
        MAPPING.put( Status.KO, "danger" );
    }

    private StatusHelper()
    {
        // no-op
    }

    public static String map( final Status status )
    {
        return MAPPING.get( status );
    }
}
