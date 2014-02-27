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

package org.apache.sirona.javaagent.logging;

import java.text.MessageFormat;

/**
 * Nothing really complicated in this logger.
 * It's just a way to have an unique place where it's done.
 */
public class SironaAgentLogging
{
    public static boolean AGENT_DEBUG = Boolean.getBoolean( "sirona.agent.debug" );

    /**
     * @param message very simple usage so we {@link java.text.MessageFormat}
     * @param objects
     */
    public static void debug( String message, Object... objects )
    {
        if ( AGENT_DEBUG )
        {

            // to prevent StackOverflowError we need to detect if this debug call is already in the call stack
            // typical case is Tomcat redirecting System.out/err to his own log handler
            // so as we instrument Tomcat this goes to a StackOverflowError



            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            int foundCall = 0;
            for ( StackTraceElement stackTraceElement : elements )
            {
                if ( SironaAgentLogging.class.getName().equals( stackTraceElement.getClassName() ) //
                    && "debug".equals( stackTraceElement.getMethodName() ) )
                {
                    foundCall++;
                }
            }

            // skip this call as we are probably in a recursive without any end
            if ( foundCall >= 2 )
            {
                return;
            }

            if ( objects != null )
            {
                System.out.println( MessageFormat.format( message, objects ) );
            }
            else
            {
                System.out.println( message );
            }
        }
    }

}
