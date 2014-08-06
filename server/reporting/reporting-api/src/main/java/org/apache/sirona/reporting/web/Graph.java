package org.apache.sirona.reporting.web;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Olivier Lamy
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
