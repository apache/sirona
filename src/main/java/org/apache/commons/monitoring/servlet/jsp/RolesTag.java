package org.apache.commons.monitoring.servlet.jsp;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.monitoring.Role;

public class RolesTag
    extends BodyTagSupport
{
    private Iterator<Role> roles;

    private String role;

    @Override
    public int doStartTag()
        throws JspException
    {
        roles = Role.getRoles().iterator();
        pageContext.setAttribute( role, roles.next() );
        return roles.hasNext() ? EVAL_BODY_BUFFERED : SKIP_BODY;
    }

    @Override
    public int doAfterBody()
        throws JspException
    {
        BodyContent body = getBodyContent();
        try
        {
            body.writeOut( getPreviousOut() );
        }
        catch ( IOException e )
        {
            throw new JspTagException( "RolesTag: " + e.getMessage() );
        }

        // clear up so the next time the body content is empty
        body.clearBody();
        if ( roles.hasNext() )
        {
            pageContext.setAttribute( role, roles.next() );
            return EVAL_BODY_AGAIN;
        }
        else
        {
            return SKIP_BODY;
        }
    }

    /**
     * Name of the pageContext attribute to contain the current role during interation
     * 
     * @param role
     */
    public void setRole( String role )
    {
        this.role = role;
    }

    @Override
    public void release()
    {
        role = "role";
    }
}
