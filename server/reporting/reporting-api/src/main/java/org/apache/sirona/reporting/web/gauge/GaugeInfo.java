package org.apache.sirona.reporting.web.gauge;

import java.io.Serializable;

/**
 * @since 0.3
 */
public class GaugeInfo
    implements Serializable
{

    private final String name;

    private final String encodedName;

    public GaugeInfo( String name, String encodedName )
    {
        this.name = name;
        this.encodedName = encodedName;
    }

    public String getName()
    {
        return name;
    }

    public String getEncodedName()
    {
        return encodedName;
    }

    @Override
    public String toString()
    {
        return "GaugeInfo{" +
            "name='" + name + '\'' +
            ", encodedName='" + encodedName + '\'' +
            '}';
    }
}
