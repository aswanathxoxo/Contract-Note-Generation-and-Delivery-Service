package com.gtl.utils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gtl.config.GlobalConfigurationReader;

import lombok.extern.log4j.Log4j2;

import java.util.Properties;

@Log4j2
@Component
public class EmailUtil {

    @Autowired
    private GlobalConfigurationReader globalConfigurationReader;

    public void sendEmail(String to, String from, String fileName, String subject, String htmlContent, String pdfFilePath) throws Exception {
        log.info("inside sendEmail");

        try {
            // Set up the SMTP server properties
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", globalConfigurationReader.getSmtpAuth());
            properties.put("mail.smtp.starttls.enable", globalConfigurationReader.getSmtpStarttls());
            properties.put("mail.smtp.host", globalConfigurationReader.getSmtpHost());
            properties.put("mail.smtp.port", globalConfigurationReader.getSmtpPort());
            log.info("properties " + properties);

            // Create a session with an authenticator
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(globalConfigurationReader.getSmtpUserName(), globalConfigurationReader.getSmtpPassword());
                }
            });
            log.info("send mail start");
            // Create a MimeMessage
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Create a multipart message
            Multipart multipart = new MimeMultipart();

            // Set the HTML content
            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html");
            multipart.addBodyPart(htmlPart);

            // Attach the PDF file
            BodyPart pdfPart = new MimeBodyPart();
            DataSource source = new FileDataSource(pdfFilePath);
            pdfPart.setDataHandler(new DataHandler(source));
            pdfPart.setFileName(fileName + ".pdf");
            multipart.addBodyPart(pdfPart);

            // Set the complete message parts
            message.setContent(multipart);

            // Send the message
            Transport.send(message);
            log.info("Email sent successfully");
        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e);
            throw e; // Optionally rethrow the exception if you want to propagate it
        } catch (Exception e) {
            log.error("An error occurred while sending email: {}", e);
            throw e; // Optionally rethrow the exception if you want to propagate it
        }
    }
}