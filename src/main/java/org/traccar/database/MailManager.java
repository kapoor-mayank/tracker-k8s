package org.traccar.database;

import java.util.Date;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.Main;
import org.traccar.model.ExtendedModel;
import org.traccar.model.User;
import org.traccar.notification.PropertiesProvider;


public final class MailManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailManager.class);

    private static Properties getProperties(PropertiesProvider provider) {
        Properties properties = new Properties();
        String host = provider.getString("mail.smtp.host");
        if (host != null) {
            properties.put("mail.transport.protocol", provider.getString("mail.transport.protocol", "smtp"));
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", String.valueOf(provider.getInteger("mail.smtp.port", 25)));

            Boolean starttlsEnable = provider.getBoolean("mail.smtp.starttls.enable");
            if (starttlsEnable != null) {
                properties.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnable));
            }
            Boolean starttlsRequired = provider.getBoolean("mail.smtp.starttls.required");
            if (starttlsRequired != null) {
                properties.put("mail.smtp.starttls.required", String.valueOf(starttlsRequired));
            }

            Boolean sslEnable = provider.getBoolean("mail.smtp.ssl.enable");
            if (sslEnable != null) {
                properties.put("mail.smtp.ssl.enable", String.valueOf(sslEnable));
            }
            String sslTrust = provider.getString("mail.smtp.ssl.trust");
            if (sslTrust != null) {
                properties.put("mail.smtp.ssl.trust", sslTrust);
            }

            String sslProtocols = provider.getString("mail.smtp.ssl.protocols");
            if (sslProtocols != null) {
                properties.put("mail.smtp.ssl.protocols", sslProtocols);
            }

            String username = provider.getString("mail.smtp.username");
            if (username != null) {
                properties.put("mail.smtp.username", username);
            }
            String password = provider.getString("mail.smtp.password");
            if (password != null) {
                properties.put("mail.smtp.password", password);
            }
            String from = provider.getString("mail.smtp.from");
            if (from != null) {
                properties.put("mail.smtp.from", from);
            }
        }
        return properties;
    }


    public void sendMessage(long userId, String subject, String body) throws MessagingException {
        sendMessage(userId, subject, body, null);
    }


    public void sendMessage(long userId, String subject, String body, MimeBodyPart attachment) throws MessagingException {
        User user = Context.getPermissionsManager().getUser(userId);

        Properties properties = null;
        if (!Context.getConfig().getBoolean("mail.smtp.ignoreUserConfig")) {
            properties = getProperties(new PropertiesProvider((ExtendedModel) user));
        }
        if (properties == null || !properties.containsKey("mail.smtp.host")) {
            properties = getProperties(new PropertiesProvider(Context.getConfig()));
        }
        if (!properties.containsKey("mail.smtp.host")) {
            LOGGER.warn("No SMTP configuration found");

            return;
        }
        Session session = Session.getInstance(properties);

        MimeMessage message = new MimeMessage(session);

        String from = properties.getProperty("mail.smtp.from");
        if (from != null) {
            message.setFrom((Address) new InternetAddress(from));
        }

        message.addRecipient(Message.RecipientType.TO, (Address) new InternetAddress(user.getEmail()));
        message.setSubject(subject);
        message.setSentDate(new Date());

        if (attachment != null) {
            MimeMultipart mimeMultipart = new MimeMultipart();

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(body, "text/html; charset=utf-8");
            mimeMultipart.addBodyPart((BodyPart) mimeBodyPart);
            mimeMultipart.addBodyPart((BodyPart) attachment);

            message.setContent((Multipart) mimeMultipart);
        } else {
            message.setContent(body, "text/html; charset=utf-8");
        }

        try (Transport transport = session.getTransport()) {
            ((StatisticsManager) Main.getInjector().getInstance(StatisticsManager.class)).registerMail();
            transport.connect(properties
                    .getProperty("mail.smtp.host"), properties
                    .getProperty("mail.smtp.username"), properties
                    .getProperty("mail.smtp.password"));
            transport.sendMessage((Message) message, message.getAllRecipients());
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\MailManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */