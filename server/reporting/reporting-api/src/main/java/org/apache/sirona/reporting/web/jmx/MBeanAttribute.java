package org.apache.sirona.reporting.web.jmx;

import java.io.Serializable;

/**
 * @since 0.3
 */
public class MBeanAttribute
    implements Serializable
{
    private final String name;

    private final String type;

    private final String description;

    private final String value;

    public MBeanAttribute( final String name, final String type, final String description, final String value )
    {
        this.name = name;
        this.type = type;
        this.value = value;
        if ( description != null )
        {
            this.description = description;
        }
        else
        {
            this.description = "No description";
        }
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getDescription()
    {
        return description;
    }

    public String getValue()
    {
        return value;
    }
}
