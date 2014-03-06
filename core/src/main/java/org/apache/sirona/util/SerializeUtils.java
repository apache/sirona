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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

/**
 *
 */
public class SerializeUtils
{
    private SerializeUtils()
    {
        // no op just an helper class
    }


    public static byte[] serialize( Object object )
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream( baos );
            oos.writeObject( object );
            oos.flush();
            oos.close();
            return baos.toByteArray();
        }
        catch ( IOException e )
        {
            // ignore as should not happen anyway log the stack trace
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T deserialize( byte[] source, final Class<T> tClass )
    {
        try
        {
            ByteArrayInputStream bis = new ByteArrayInputStream( source );
            ObjectInputStream ois = new ObjectInputStream( bis )
            {

                @Override
                protected Class<?> resolveClass( ObjectStreamClass objectStreamClass )
                    throws IOException, ClassNotFoundException
                {
                    return tClass;
                }

            };
            T obj = tClass.cast( ois.readObject() );
            ois.close();
            return obj;
        }
        catch ( Exception e )
        {
            // ignore as should not happen anyway log the stack trace
            e.printStackTrace();
        }

        return null;
    }
}
