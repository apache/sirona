package org.apache.sirona.reporting.web.jmx;

import java.io.Serializable;
import java.util.List;

/**
 * @author Olivier Lamy
 */
public class JMXInvocationRequest
    implements Serializable
{

    private String mbeanEncodedName;

    private String operationName;

    private List<String> parameters;

    public JMXInvocationRequest()
    {
        // no op
    }

    public JMXInvocationRequest( String mbeanEncodedName, String operationName, List<String> parameters )
    {
        this.mbeanEncodedName = mbeanEncodedName;
        this.operationName = operationName;
        this.parameters = parameters;
    }

    public String getMbeanEncodedName()
    {
        return mbeanEncodedName;
    }

    public void setMbeanEncodedName( String mbeanEncodedName )
    {
        this.mbeanEncodedName = mbeanEncodedName;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public void setOperationName( String operationName )
    {
        this.operationName = operationName;
    }

    public List<String> getParameters()
    {
        return parameters;
    }

    public void setParameters( List<String> parameters )
    {
        this.parameters = parameters;
    }

    @Override
    public String toString()
    {
        return "JMXInvocationRequest{" +
            "mbeanEncodedName='" + mbeanEncodedName + '\'' +
            ", operationName='" + operationName + '\'' +
            ", parameters=" + parameters +
            '}';
    }
}
