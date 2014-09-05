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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class JMXNode
    implements Serializable
{

    private final String name;

    private final String label;

    private final Map<Key, JMXNode> children = new TreeMap<Key, JMXNode>();

    private String base64 = null;

    public JMXNode( final String name )
    {
        this.name = name;
        this.label = name;
    }

    public static void addNode( final JMXNode rootNode, final String domain, final String props )
    {
        final Map<String, String> properties = new TreeMap<String, String>( new JMXPropertiesComparator( props ) );
        for ( final String k : props.split( "," ) )
        {
            final String[] kv = k.split( "=" );
            if ( kv.length < 2 )
            {
                properties.put( StringEscapeUtils.escapeHtml4( kv[0] ), "" );
            }
            else
            {
                properties.put( StringEscapeUtils.escapeHtml4( kv[0] ), StringEscapeUtils.escapeHtml4( kv[1] ) );
            }
        }

        final Key rootKey = new Key( "domain", domain );
        JMXNode node = rootNode.children.get( rootKey );
        if ( node == null )
        {
            node = new JMXNode( domain );
            rootNode.children.put( rootKey, node );
        }

        for ( final Map.Entry<String, String> entry : properties.entrySet() )
        {
            final Key key = new Key( entry.getKey(), entry.getValue() );
            final String value = entry.getValue();

            JMXNode child = node.children.get( key );
            if ( child == null )
            {
                child = new JMXNode( value );
                node.children.put( key, child );
            }

            node = child;
        }

        node.base64 = Base64.encodeBase64URLSafeString( ( domain + ":" + props ).getBytes() );
    }

    public String getName()
    {
        return name;
    }

    public boolean isLeaf()
    {
        return base64 != null;
    }

    public String getBase64()
    {
        return base64;
    }

    public String getLabel()
    {
        return label;
    }

    public Collection<JMXNode> getChildren()
    {
        return Collections.unmodifiableCollection( children.values() );
    }

    protected static class Key
        implements Comparable<Key>, Serializable
    {
        private final String key;

        private final String value;

        public Key( final String key, final String value )
        {
            this.key = key;
            this.value = value;
        }

        public String getKey()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }

        @Override
        public boolean equals( final Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( o == null || Key.class != o.getClass() )
            {
                return false;
            }

            final Key key1 = Key.class.cast( o );
            return key.equals( key1.key ) && value.equals( key1.value );
        }

        @Override
        public int hashCode()
        {
            int result = key.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }

        @Override
        public int compareTo( final Key o )
        {
            if ( equals( o ) )
            {
                return 0;
            }

            final int keys = key.compareTo( o.key );
            if ( keys != 0 )
            {
                return keys;
            }
            return value.compareTo( o.value );
        }

        @Override
        public String toString()
        {
            return "{" + key + " = " + value + '}';
        }
    }

    protected static class JMXPropertiesComparator
        implements Comparator<String>
    {
        private final String properties;

        protected JMXPropertiesComparator( final String props )
        {
            properties = props;
        }

        @Override
        public int compare( final String o1, final String o2 )
        {
            if ( o1.equals( o2 ) )
            {
                return 0;
            }

            if ( "type".equals( o1 ) )
            {
                return -1;
            }
            if ( "type".equals( o2 ) )
            {
                return 1;
            }
            if ( "j2eeType".equals( o1 ) )
            {
                return -1;
            }
            if ( "j2eeType".equals( o2 ) )
            {
                return 1;
            }

            return properties.indexOf( o1 + "=" ) - properties.indexOf( o2 + "=" );
        }
    }
}
