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

import org.apache.sirona.status.Status;
import org.apache.sirona.status.Validation;
import org.apache.sirona.status.ValidationResult;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataSourceValidation implements Validation {
    private static final Logger LOGGER = Logger.getLogger(DataSourceValidation.class.getName());

    private final DataSource ds;
    private final String name;
    private final String query;

    public DataSourceValidation(final DataSource ds, final String name, final String validationQuery) {
        this.ds = ds;
        this.name = "tomee-datasource-" + name;
        this.query = validationQuery;
    }

    @Override
    public ValidationResult validate() {
        Connection c = null;
        try {
            c = ds.getConnection();
            final PreparedStatement statement = c.prepareStatement(query);
            try {
                if (!statement.execute()) {
                    return new ValidationResult(name, Status.KO, "validation query didn't execute correctly");
                }
                return new ValidationResult(name, Status.OK, "validation query executed correctly");
            } finally {
                statement.close();
            }
        } catch (final SQLException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            return new ValidationResult(name, Status.KO, e.getMessage());
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
}
