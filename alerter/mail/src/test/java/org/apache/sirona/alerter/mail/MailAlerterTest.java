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
package org.apache.sirona.alerter.mail;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;
import org.apache.sirona.alert.AlertListener;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.junit.Rule;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MailAlerterTest {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(new ServerSetup(1235, "localhost", "smtp"));

    @Test
    public void run() throws MessagingException, IOException {
        final MailAlerter listener = new MailAlerter();
        listener.setFrom("me@here.asf");
        listener.setPort(Integer.toString(greenMail.getSmtp().getPort()));
        listener.setHost("localhost");
        listener.setTo("target@mock.com");
        listener.init();
        listener.onAlert(new AlertListener.Alert("node1", new NodeStatus(
            new ValidationResult[]{ new ValidationResult("val", Status.OK, "msg")}, new Date())) {});

        greenMail.waitForIncomingEmail(1);
        final MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("me@here.asf", messages[0].getFrom()[0].toString());
        assertEquals(1, messages[0].getAllRecipients().length);
        assertEquals("target@mock.com", messages[0].getAllRecipients()[0].toString());
        assertTrue(messages[0].getContent().toString().contains("node1 throw an alert for:\r\n\r\nval;msg;OK\r\n"));
    }
}
