/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sirona.tomee.agent.validation;

import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.status.Validation;
import org.apache.sirona.status.ValidationFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TomEEValidationFactory implements ValidationFactory {
    private static final Logger LOGGER = Logger.getLogger(TomEEValidationFactory.class.getName());

    @Override
    public Validation[] validations() {
        final Collection<Validation> validations = new LinkedList<Validation>();

        if (Configuration.is(Configuration.CONFIG_PROPERTY_PREFIX + "tomee.validations.activated", true)) {
            final Context jndiContext = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
            final OpenEjbConfiguration configuration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);

            for (final ResourceInfo o : configuration.facilities.resources) {
                boolean resource = false;
                for (final String type : o.types) {
                    if (type.toLowerCase(Locale.ENGLISH).contains("datasource")) {
                        resource = true;
                        break;
                    }
                }

                if (resource && !o.id.endsWith("NonJta")) { // NonJta are automatically created when missing and are a copy of the jta one
                    final String jndi;
                    if (o.id.startsWith("java:global")) {
                        jndi = o.id;
                    } else {
                        jndi = "openejb:Resource/" + o.id;
                    }


                    try {
                        final DataSource ds = DataSource.class.cast(jndiContext.lookup(jndi));
                        String validationQuery = o.properties.getProperty("ValidationQuery");
                        if (validationQuery == null || validationQuery.trim().isEmpty()) {
                            Connection c = null;
                            try {
                                c = ds.getConnection();
                                if (validationQuery == null || validationQuery.trim().isEmpty()) {
                                    validationQuery = defaultValidationQuery(c.getMetaData().getDatabaseProductName());
                                }
                                if (validationQuery == null) {
                                    continue;
                                }
                            } catch (final SQLException e) {
                                LOGGER.log(Level.INFO, e.getMessage());
                            } finally {
                                try {
                                    if (c != null) {
                                        c.close();
                                    }
                                } catch (final Exception e) {
                                    // no-op
                                }
                            }
                        }

                        validations.add(new DataSourceValidation(ds, o.id, validationQuery));
                    } catch (final NamingException e) {
                        LOGGER.log(Level.INFO, e.getMessage());
                    }
                }
            }
        }

        return validations.toArray(new Validation[validations.size()]);
    }

    private static String defaultValidationQuery(final String db) {
        if (db.toLowerCase(Locale.ENGLISH).contains("mysql") || db.toLowerCase(Locale.ENGLISH).contains("h2") ||
            db.toLowerCase(Locale.ENGLISH).contains("sqlite") || db.toLowerCase(Locale.ENGLISH).contains("postgres")
            || db.toLowerCase(Locale.ENGLISH).contains("sqlserver") || db.toLowerCase(Locale.ENGLISH).contains("derby")) {
            return "select 1";
        } else if (db.toLowerCase(Locale.ENGLISH).contains("oracle")) {
            return "select 1 form dual";
        } else if (db.toLowerCase(Locale.ENGLISH).contains("hsql")) {
            return "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS";
        }
        return null;
    }

}
