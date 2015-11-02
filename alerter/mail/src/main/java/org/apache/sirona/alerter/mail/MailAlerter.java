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

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.sirona.alert.AlertListener;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Created;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public class MailAlerter implements AlertListener {
    private String template;
    private String subjectTemplate;

    private boolean useDefault;
    private InternetAddress from;
    private String protocol;
    private String host;
    private String user;
    private String password;
    private int port;
    private int timeout;
    private boolean tls;
    private boolean auth;

    private volatile boolean init = false;
    private Session session;
    private Collection<InternetAddress> recipients;

    @Created
    public void init() {
        // validations
        if (from == null) {
            throw new IllegalArgumentException("Missing 'from'");
        }
        if (host == null) {
            throw new IllegalArgumentException("Missing 'host'");
        }
        if (port == 0) {
            throw new IllegalArgumentException("Missing 'port'");
        }
        if (recipients == null || recipients.size() == 0) {
            throw new IllegalArgumentException("Missing 'to'");
        }

        // some defaults
        if (protocol == null) {
            protocol = "smtp";
        }
        if (template == null) {
            template = "${marker} throw an alert for:\n\n${resultsCsv}\n";
        }
        if (subjectTemplate == null) {
            subjectTemplate = "${marker} throw an alert at ${date}";
        }

        // actual init
        final Properties properties = new Properties();
        properties.setProperty("mail.protocol.protocol", protocol);
        properties.setProperty("mail." + protocol + ".host", host);
        properties.setProperty("mail." + protocol + ".port", Integer.toString(port));
        if (tls) {
            properties.setProperty("mail." + protocol + ".starttls.enable", "true");
        }
        if (user != null) {
            properties.setProperty("mail." + protocol + ".user", user);
        }
        if (password != null) {
            properties.setProperty("password", password);
        }
        if (auth || password != null) {
            properties.setProperty("mail." + protocol + ".auth", "true");
        }
        if (timeout > 0) {
            properties.setProperty("mail." + protocol + ".timeout", Integer.toString(timeout));
        }

        final String password = properties.getProperty("password");

        Authenticator auth = null;
        if (password != null) {
            final String protocol = properties.getProperty("mail.protocol.protocol", "smtp");

            String user = properties.getProperty("mail." + protocol + ".user");
            if (user == null) {
                user = properties.getProperty("mail.user");
            }

            if (user != null) {
                final PasswordAuthentication pa = new PasswordAuthentication(user, password);
                auth = new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return pa;
                    }
                };
            }
        }

        if (useDefault) {
            if (auth != null) {
                session = Session.getDefaultInstance(properties, auth);
            } else {
                session = Session.getDefaultInstance(properties);
            }
        } else if (auth != null) {
            session = Session.getInstance(properties, auth);
        } else {
            session = Session.getInstance(properties);
        }
    }

    @Override
    public void onAlert(final Alert alert) {
        try {
            Transport.send(buildMessage(alert.asMap()));
        } catch (final MessagingException e) {
            throw new IllegalStateException(e);
        }
    }

    private MimeMessage buildMessage(final Map<String, Object> placeholders) throws MessagingException {
        final MimeMessage message = new MimeMessage(session);
        message.setFrom(from);
        message.setSubject(StrSubstitutor.replace(subjectTemplate, placeholders));
        message.setText(StrSubstitutor.replace(template, placeholders));
        for (final InternetAddress recipient : recipients) {
            message.addRecipient(Message.RecipientType.TO, recipient);
        }
        return message;
    }

    public void setTemplate(final String template) {
        this.template = StrSubstitutor.replace(template, Configuration.properties());
    }

    public void setFrom(final String from) {
        try {
            this.from = new InternetAddress(from);
        } catch (final AddressException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setPassword(final String password) {
        this.password = StrSubstitutor.replace(password, Configuration.properties());
    }

    public void setTo(final String to) {
        recipients = new LinkedList<InternetAddress>();
        if (to != null) {
            try {
                for (final String recipient : to.split(" *, *")) {
                    recipients.add(new InternetAddress(recipient));
                }
            } catch (final AddressException ae) {
                throw new IllegalArgumentException(ae);
            }
        }
    }

    public void setSubjectTemplate(final String subjectTemplate) {
        this.subjectTemplate = StrSubstitutor.replace(subjectTemplate, Configuration.properties());
    }

    public void setUseDefault(final boolean useDefault) {
        this.useDefault = useDefault;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public void setPort(final String port) {
        this.port = Integer.parseInt(StrSubstitutor.replace(port, Configuration.properties()));
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public void setTls(final boolean tls) {
        this.tls = tls;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public void setInit(boolean init) {
        this.init = init;
    }
}
