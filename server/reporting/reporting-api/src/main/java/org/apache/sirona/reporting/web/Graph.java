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
package org.apache.sirona.reporting.web;

import java.io.Serializable;
import java.util.Map;

/**
 * @since 0.3
 */
public class Graph
    implements Serializable
{

    public static final String DEFAULT_COLOR = "#317eac";

    private String label;

    private String color;

    private Map<Long, Double> data;

    public Graph()
    {
    }

    public Graph( String label, String color, Map<Long, Double> data )
    {
        this.label = label;
        this.color = color;
        this.data = data;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel( String label )
    {
        this.label = label;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor( String color )
    {
        this.color = color;
    }

    public Map<Long, Double> getData()
    {
        return data;
    }

    public void setData( Map<Long, Double> data )
    {
        this.data = data;
    }
}
