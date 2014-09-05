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
package org.apache.sirona.reporting.web.jmx;

import java.util.List;

/**
 * @since 0.3
 */
public class MBeanInformations
{

    private final String objectName;

    private final String objectNameHash;

    private final String classname;

    private final String description;

    private final List<MBeanAttribute> attributes;

    private final List<MBeanOperation> operations;

    public MBeanInformations( final String objectName, //
                              final String objectNameHash, //
                              final String classname, //
                              final String description, //
                              final List<MBeanAttribute> attributes, //
                              final List<MBeanOperation> operations )
    {
        this.objectName = objectName;
        this.objectNameHash = objectNameHash;
        this.classname = classname;
        this.description = description;
        this.attributes = attributes;
        this.operations = operations;
    }

    public String getObjectName()
    {
        return objectName;
    }

    public String getObjectNameHash()
    {
        return objectNameHash;
    }

    public String getClassname()
    {
        return classname;
    }

    public String getDescription()
    {
        return description;
    }

    public List<MBeanAttribute> getAttributes()
    {
        return attributes;
    }

    public List<MBeanOperation> getOperations()
    {
        return operations;
    }
}
