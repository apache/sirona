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

import java.io.Serializable;

/**
 * @author Olivier Lamy
 * @since 0.3
 */
public class ValidationResultInfo
    implements Serializable
{

    private final String status;

    private final String statusLabel;

    private final String message;

    private final String name;

    public ValidationResultInfo( String status, String statusLabel, String message, String name )
    {
        this.status = status;
        this.statusLabel = statusLabel;
        this.message = message;
        this.name = name;
    }

    public String getStatus()
    {
        return status;
    }

    public String getStatusLabel()
    {
        return statusLabel;
    }

    public String getMessage()
    {
        return message;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "ValidationResultInfo{" +
            "status='" + status + '\'' +
            ", message='" + message + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
