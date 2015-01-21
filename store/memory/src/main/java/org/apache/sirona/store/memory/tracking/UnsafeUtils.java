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
package org.apache.sirona.store.memory.tracking;

import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

import java.lang.reflect.Field;

/**
 * FIXME move that to store memory as only used for path tracking in memory
 */
@IgnoreJRERequirement
public class UnsafeUtils
{
    private static sun.misc.Unsafe UNSAFE;


    static
    {
        try
        {
            Field f = sun.misc.Unsafe.class.getDeclaredField( "theUnsafe" );
            f.setAccessible( true );
            UNSAFE = (sun.misc.Unsafe) f.get( null );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    private UnsafeUtils()
    {
        // no op
    }

    public static sun.misc.Unsafe getUnsafe()
    {
        return UNSAFE;
    }
}
