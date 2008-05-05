package org.apache.commons.monitoring.servlet.jsp;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.servlet.ServletContextUtil;

public abstract class AbstractSelectTag
    extends TagSupport
{
    private String id;

    private String name;

    String repository;

    public AbstractSelectTag()
    {
        super();
    }

    protected Repository getRepository()
        throws JspException
    {
        return TagUtils.getRepository( pageContext, repository );
    }

    @Override
    public int doEndTag()
        throws JspException
    {
        StringBuffer out = new StringBuffer( "<select" );
        TagUtils.setAttribute( out, "name", name );
        TagUtils.setAttribute( out, "id", id );
        out.append( ">" );
        List<String> categories = new LinkedList<String>( getElements() );
        Collections.sort( categories );
        for ( String category : categories )
        {
            out.append( "<option value='" );
            out.append( category );
            out.append( "'>" );
            out.append( category );
            out.append( "</option>" );
        }

        out.append( "</select>" );
        try
        {
            pageContext.getOut().print( out.toString() );
        }
        catch ( Exception e )
        {
            throw new JspTagException( "CategoriesTag : " + e.getMessage() );
        }

        return EVAL_PAGE;
    }

    /**
     * The set of elements to display as a select list
     *
     * @return
     */
    protected abstract Collection<? extends String> getElements()
        throws JspException;

    public void setId( String id )
    {
        this.id = id;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository( String repository )
    {
        this.repository = repository;
    }

}