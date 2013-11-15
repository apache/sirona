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
package org.apache.sirona.plugin.hazelcast.agent.instance;

import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastMemberFactory {
    /**
     * @param name name of the instance in sirona.properties
     * @param prefix could be used to get further config
     * @return the hazelcast instance built from hazelcast.xml + instancename = name
     */
    public static HazelcastInstance newMember(final String name, final String prefix) {
        final HazelcastInstance hazelcastInstanceByName = Hazelcast.getHazelcastInstanceByName(name);
        if (hazelcastInstanceByName != null) {
            return hazelcastInstanceByName;
        }
        return Hazelcast.newHazelcastInstance(new XmlConfigBuilder().build().setInstanceName(name));
    }

    private HazelcastMemberFactory() {
        // no-op
    }
}
