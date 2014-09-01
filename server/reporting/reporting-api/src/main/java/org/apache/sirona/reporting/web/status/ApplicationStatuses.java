package org.apache.sirona.reporting.web.status;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Olivier Lamy
 * @since 0.3
 */
public class ApplicationStatuses
    implements Serializable
{

    private final String name;

    private final Collection<NodeStatusInfo> nodeStatusInfos;

    public ApplicationStatuses( String name, Collection<NodeStatusInfo> nodeStatusInfos )
    {
        this.name = name;
        this.nodeStatusInfos = nodeStatusInfos;
    }

    public String getName()
    {
        return name;
    }

    public Collection<NodeStatusInfo> getNodeStatusInfos()
    {
        return nodeStatusInfos;
    }

    @Override
    public String toString()
    {
        return "ApplicationStatuses{" +
            "name='" + name + '\'' +
            ", nodeStatusInfo=" + nodeStatusInfos +
            '}';
    }
}
