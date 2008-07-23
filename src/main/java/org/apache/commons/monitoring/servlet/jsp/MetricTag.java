package org.apache.commons.monitoring.servlet.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Role;

public class MetricTag
    extends TagSupport
{
    private String roleAttrName;

    @Override
    public int doStartTag()
        throws JspException
    {
        Role role = (Role) pageContext.getAttribute( roleAttrName );

        String name = role.getName();
        StringBuffer stb = new StringBuffer();
        if ( role.getType() == Counter.class )
        {
            checkbox( name, "hits", stb );
            checkbox( name, "total", stb );
        }
        checkbox( name, "value", stb );
        checkbox( name, "min", stb );
        checkbox( name, "max", stb );
        checkbox( name, "mean", stb );
        checkbox( name, "deviation", stb );

        return EVAL_PAGE;
    }

    private void checkbox( String name, String property, StringBuffer stb )
    {
        stb.append( "<li>" );
        stb.append( "<input type='checkbox' name='" );
        stb.append( property );
        stb.append( "' id='" );
        stb.append( name );
        stb.append( "." );
        stb.append( property );
        stb.append( "'/>" );
        stb.append( "<label for='" );
        stb.append( "'>" );
        stb.append( name );
        stb.append( "." );
        stb.append( property );
        stb.append( "</label>" );
        stb.append( "</li>" );
    }

    public void setRole( String role )
    {
        this.roleAttrName = role;
    }

}
