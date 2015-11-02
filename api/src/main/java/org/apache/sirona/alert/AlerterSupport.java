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
package org.apache.sirona.alert;

import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.Status;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AlerterSupport {
    protected final Collection<AlertListener> listeners = new CopyOnWriteArraySet<AlertListener>();

    public void notify(final Map<String, NodeStatus> nodeStatus) {
        if (nodeStatus == null) {
            return;
        }
        for (final Map.Entry<String, NodeStatus> entry : nodeStatus.entrySet()) {
            final NodeStatus status = entry.getValue();
            if (status.getStatus() != Status.OK) {
                final AlertListener.Alert alert = new AlertListener.Alert(entry.getKey(), status);
                for (final AlertListener listener : listeners) {
                    try {
                        listener.onAlert(alert);
                    } catch (final RuntimeException ex) {
                        Logger.getLogger(AlerterSupport.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            }
        }
    }

    public void addAlerter(final AlertListener listener) {
        listeners.add(listener);
    }

    public void removeAlerter(final AlertListener listener) {
        listeners.remove(listener);
    }

    public boolean hasAlerter() {
        return !listeners.isEmpty();
    }
}
