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

import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.sirona.javaagent.InJvmTransformerRunner;
import org.apache.sirona.repositories.Repository;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.FileArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

@RunWith(InJvmTransformerRunner.class)
public class OpenEJBTest {
    @Before
    @After
    public void reset() {
        Repository.INSTANCE.reset();
    }

    @Test
    public void scanning() throws MalformedURLException {
        final AnnotationFinder finder = //
            new AnnotationFinder( new FileArchive( Thread.currentThread().getContextClassLoader(), //
                                                   JarLocation.jarLocation( TicTacToeServiceEJB.class ) //
                                                       .toURI() //
                                                       .toURL() )//
            ).link();
        assertEquals( 1, finder.findMetaAnnotatedFields( PersistenceContext.class ).size() );
    }

    @Test
    public void checkEJBAreInstrumented() throws Throwable {
        assertEquals(0, Repository.INSTANCE.counters().size());
        TicTacToeServiceEJB.class.cast(LocalBeanProxyFactory.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        return method.invoke(new TicTacToeServiceEJB(), args);
                    }
                }, TicTacToeServiceEJB.class))
                .touch();
        assertEquals(2, Repository.INSTANCE.counters().size());
    }

    @Singleton
    @Lock(LockType.READ)
    public static class TicTacToeServiceEJB {
        @PersistenceContext(unitName = "tic-tac-toe")
        private EntityManager em;

        @Inject
        private Integer size;

        public void touch() {
            // no-op
        }

        public Square play(final String partyId) {
            return computerMove(partyId);
        }

        private Square computerMove(final String partyId) {
            final Square[][] table = gameAsArray(partyId);
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (table[i][j] == null) {
                        return Square.newSquare(partyId, Square.Player.O, i + 1, j + 1);
                    }
                }
            }
            return null;
        }

        @TransactionAttribute(TransactionAttributeType.SUPPORTS)
        public boolean hasPlayer(final String partyId, final int x, final int y) {
            try {
                final Square square = em.createNamedQuery("Square.findAllByPartyAndPosition", Square.class)
                        .setParameter("partyId", partyId)
                        .setParameter("x", x)
                        .setParameter("y", y)
                        .getSingleResult();
                return square.getPlayer() != null;
            } catch (final NoResultException nre) {
                return false;
            }
        }

        public boolean hasWon(final Square square) {
            final Square.Player player = square.getPlayer();
            final int x = square.getX() - 1;
            final int y = square.getY() - 1;

            final Square[][] array = gameAsArray(square.getPartyId());

            for (int i = 0; i < size; i++) { // vertical
                if (!isTheSamePlayer(player, array[x][i])) {
                    break;
                }
                if (i == size - 1) {
                    return true;
                }
            }
            for (int i = 0; i < size; i++) { // horizontal
                if (!isTheSamePlayer(player, array[i][y])) {
                    break;
                }
                if (i == size - 1) {
                    return true;
                }
            }
            if (x == y) { // diagonal
                for (int i = 0; i < size; i++) {
                    if (!isTheSamePlayer(player, array[i][i])) {
                        break;
                    }
                    if (i == size - 1) {
                        return true;
                    }
                }
            } else if (x == size - y - 1) { // anti-diagonal
                for (int i = 0; i < size; i++) {
                    final int xtmp = size - i - 1;
                    if (!isTheSamePlayer(player, array[xtmp][i])) {
                        break;
                    }
                    if (i == size - 1) {
                        return true;
                    }
                }
            }

            return false;
        }

        public void savePosition(final Square square) {
            em.persist(square);
        }

        private boolean isTheSamePlayer(final Square.Player player, final Square square) {
            return square != null && square.getPlayer() != null && square.getPlayer().equals(player);
        }

        public Square[][] gameAsArray(final String partyId) {
            final List<Square> entities = em.createNamedQuery("Square.findAllByParty", Square.class)
                    .setParameter("partyId", partyId)
                    .getResultList();

            final Square[][] table = new Square[size][];
            for (int i = 0; i < size; i++) {
                table[i] = new Square[size];
            }

            if (entities != null) {
                for (final Square s : entities) {
                    table[s.getX() - 1][s.getY() - 1] = s;
                }
            }

            return table;
        }

        public Collection<Square> findAll() {
            final List<Square> list = em.createNamedQuery("Square.findAll", Square.class).getResultList();
            if (list == null) {
                return emptyList();
            }
            return list;
        }
    }

    public static class Square {
        public static enum Player {
            O, X
        }

        private long id;
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

        public static Square newSquare(final String partyId, final Square.Player player, final int x, final int y) {
            final Square newServiceSquare = new Square();
            newServiceSquare.setX(x);
            newServiceSquare.setY(y);
            newServiceSquare.setPartyId(partyId);
            newServiceSquare.setPlayer(player);
            return newServiceSquare;
        }
    }
}
