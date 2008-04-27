package org.apache.commons.monitoring.servlet.jsp;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.monitoring.Monitoring;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.servlet.ServletContextUtil;

public abstract class AbstractSelectTag
    extends TagSupport
{
    protected Repository repository;
    
    private String id;
    
    private String name;

    public AbstractSelectTag()
    {
        super();
    }

    @Override
    public int doEndTag()
        throws JspException
    {
        if (repository == null)
        {
            repository = (Repository) pageContext.getAttribute( ServletContextUtil.REPOSITORY_KEY );
        }
        
        StringBuffer stb = new StringBuffer( "<select" );
        if ( id != null )
        {
            stb.append( " id='" ).append( id ).append( "'" );
        }
        if ( name != null )
        {
            stb.append( " name='" ).append( name ).append( "'" );
        }
        stb.append( ">" );
        List<String> categories = new LinkedList<String>( getElements() );
        Collections.sort( categories );
        for ( String category : categories )
        {
            stb.append( "<option value='" );
            stb.append( category );
            stb.append( "'>" );
            stb.append( category );
            stb.append( "</option>" );
        }
        
        stb.append( "</select>" );
        try
        {
            pageContext.getOut().print( stb.toString() );
        }
        catch ( Exception e )
        {
            throw new JspTagException( "CategoriesTag : " + e.getMessage() );
        }
    
        return EVAL_PAGE;
    }

    /**
     * The set of elements to display as a select list
     * @return
     */
    protected abstract  Collection<? extends String> getElements();

    public void setId( String id )
    {
        this.id = id;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setRepository( String repository )
    {
        this.repository = (Repository) pageContext.getAttribute( repository );
    }

}