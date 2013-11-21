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

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.security.UsernamePasswordCredentials;
import org.apache.sirona.configuration.Configuration;

public class HazelcastClientFactory {
    public static HazelcastInstance newClient(final String prefix) {
        final ClientConfig config = new XmlClientConfigBuilder().build();

        final String addresses = Configuration.getProperty(prefix + "addresses", null);
        if (addresses != null) {
            for (final String address : addresses.split(",")) {
                config.addAddress(address.trim());
            }
        }

        final String credentials = Configuration.getProperty(prefix + "credentials", null);
        if (credentials != null) {
            final String[] segments = credentials.split(":");
            config.setCredentials(new UsernamePasswordCredentials(segments[0], segments[1]));
        }

        return HazelcastClient.newHazelcastClient(config);
    }

    private HazelcastClientFactory() {
        // no-op
    }
}
