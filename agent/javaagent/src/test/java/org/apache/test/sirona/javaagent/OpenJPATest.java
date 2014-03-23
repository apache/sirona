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
package org.apache.test.sirona.javaagent;

import org.apache.openjpa.enhance.PCClassFileTransformer;
import org.apache.sirona.javaagent.InJvmTransformerRunner;
import org.apache.sirona.javaagent.SironaTransformer;
import org.apache.sirona.javaagent.Transformers;
import org.apache.sirona.repositories.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Transformers({PCClassFileTransformer.class, SironaTransformer.class })
@RunWith(InJvmTransformerRunner.class)
public class OpenJPATest {
    @Before
    @After
    public void reset() {
        Repository.INSTANCE.reset();
    }

    @Test
    public void checkTheEntityIsUsableAndTransformationDidntFail() throws Throwable {
        assertEquals(0, Repository.INSTANCE.counters().size());
        final ServiceSquareEntity serviceSquareEntity = new ServiceSquareEntity();
        serviceSquareEntity.setPlayer(ServiceSquareEntity.Player.O);
        assertEquals("O", serviceSquareEntity.getPlayer().name());
        assertEquals(5, Repository.INSTANCE.counters().size());
    }

    @Entity
    @NamedQueries({
            @NamedQuery(
                    name = "ServiceSquare.findAll",
                    query = "select i from ServiceSquare i"),
            @NamedQuery(
                    name = "ServiceSquare.findAllByParty",
                    query = "select i from ServiceSquare i where i.partyId = :partyId"),
            @NamedQuery(
                    name = "ServiceSquare.findAllByPartyAndPosition",
                    query = "select i from ServiceSquare i where i.partyId = :partyId and i.x = :x and i.y = :y"),
    })
    public static class ServiceSquareEntity {
        public static enum Player {
            O, X
        }

        @Id
        @GeneratedValue
        private long id;

        @Enumerated(EnumType.STRING)
        private Player player;

        private String partyId;
        private int x;
        private int y;

        public long getId() {
            return id;
        }

        public String getPartyId() {
            return partyId;
        }

        public void setPartyId(final String partyId) {
            this.partyId = partyId;
        }

        public Player getPlayer() {
            return player;
        }

        public void setPlayer(final Player player) {
            this.player = player;
        }

        public int getX() {
            return x;
        }

        public void setX(final int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(final int y) {
            this.y = y;
        }

        public static ServiceSquareEntity newSquare(final String partyId, final ServiceSquareEntity.Player player, final int x, final int y) {
            final ServiceSquareEntity newServiceSquareEntity = new ServiceSquareEntity();
            newServiceSquareEntity.setX(x);
            newServiceSquareEntity.setY(y);
            newServiceSquareEntity.setPartyId(partyId);
            newServiceSquareEntity.setPlayer(player);
            return newServiceSquareEntity;
        }
    }
}
