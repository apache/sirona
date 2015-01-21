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

import java.util.Iterator;

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

    @Override
    public Iterator<S> iterator()
    {
        try
        {
            return java.util.ServiceLoader.load( this.service, this.loader ).iterator();
        } catch ( Throwable t)
        {
            t.printStackTrace();
            return null;
        }
    }
}
