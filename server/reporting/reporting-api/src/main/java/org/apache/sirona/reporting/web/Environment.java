/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sirona.reporting.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

/**
 * @since 0.3
 */
@Path( "/environment" )
public class Environment
{

    @GET
    @Path( "/os" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public Os getOs()
    {
        final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        return new Os( os.getArch(), os.getName(), os.getVersion(), os.getAvailableProcessors() );
    }

    @GET
    @Path( "/memory" )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    public Memory getMemory()
    {
        final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        return new Memory( memory.getHeapMemoryUsage().getMax(), //
                           memory.getHeapMemoryUsage().getInit(), //
                           memory.getNonHeapMemoryUsage().getMax(), //
                           memory.getNonHeapMemoryUsage().getInit() );
    }


    public static class Os
        implements Serializable
    {
        private String arch;

        private String name;

        private String version;

        private int numberProcessor;

        public Os()
        {
            // no op
        }

        public Os( String arch, String name, String version, int numberProcessor )
        {
            this.arch = arch;
            this.name = name;
            this.version = version;
            this.numberProcessor = numberProcessor;
        }

        public String getArch()
        {
            return arch;
        }

        public void setArch( String arch )
        {
            this.arch = arch;
        }

        public String getName()
        {
            return name;
        }

        public void setName( String name )
        {
            this.name = name;
        }

        public String getVersion()
        {
            return version;
        }

        public void setVersion( String version )
        {
            this.version = version;
        }

        public int getNumberProcessor()
        {
            return numberProcessor;
        }

        public void setNumberProcessor( int numberProcessor )
        {
            this.numberProcessor = numberProcessor;
        }
    }

    public static class Memory
        implements Serializable
    {
        private long maxMemory;

        private long initMemory;

        private long maxNonHeapMemory;

        private long initNonHeapMemory;

        public Memory()
        {
            // no op
        }

        public Memory( long maxMemory, long initMemory, long maxNonHeapMemory, long initNonHeapMemory )
        {
            this.maxMemory = maxMemory;
            this.initMemory = initMemory;
            this.maxNonHeapMemory = maxNonHeapMemory;
            this.initNonHeapMemory = initNonHeapMemory;
        }

        public long getMaxMemory()
        {
            return maxMemory;
        }

        public void setMaxMemory( long maxMemory )
        {
            this.maxMemory = maxMemory;
        }

        public long getInitMemory()
        {
            return initMemory;
        }

        public void setInitMemory( long initMemory )
        {
            this.initMemory = initMemory;
        }

        public long getMaxNonHeapMemory()
        {
            return maxNonHeapMemory;
        }

        public void setMaxNonHeapMemory( long maxNonHeapMemory )
        {
            this.maxNonHeapMemory = maxNonHeapMemory;
        }

        public long getInitNonHeapMemory()
        {
            return initNonHeapMemory;
        }

        public void setInitNonHeapMemory( long initNonHeapMemory )
        {
            this.initNonHeapMemory = initNonHeapMemory;
        }
    }
}
