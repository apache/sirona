package org.apache.sirona.cassandra.pathtracking;

import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.tracking.PathTrackingEntry;

import java.util.Date;
import java.util.List;

/**
 * @author Olivier Lamy
 */
public class CassandraPathTrackingDataStore
    implements PathTrackingDataStore
{

    @Override
    public void store( PathTrackingEntry pathTrackingEntry )
    {

    }

    @Override
    public List<PathTrackingEntry> retrieve( String trackingId )
    {
        return null;
    }

    @Override
    public List<String> retrieveTrackingIds( Date startTime, Date endTime )
    {
        return null;
    }
}
