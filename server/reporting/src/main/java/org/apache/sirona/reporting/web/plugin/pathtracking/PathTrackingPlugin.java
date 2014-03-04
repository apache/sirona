package org.apache.sirona.reporting.web.plugin.pathtracking;

import org.apache.sirona.reporting.web.plugin.api.Plugin;

/**
 *
 */
public class PathTrackingPlugin
    implements Plugin
{

    @Override
    public String name()
    {
        return "PathTracking";
    }

    @Override
    public Class<?> endpoints()
    {
        return PathTrackingEndpoints.class;
    }

    @Override
    public String mapping()
    {
        return "/pathtracking";
    }
}
