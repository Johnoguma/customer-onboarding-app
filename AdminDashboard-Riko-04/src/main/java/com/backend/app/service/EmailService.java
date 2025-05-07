package com.backend.app.service;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    @Autowired private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message);

        try {
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom("echoge11@gmail.com");
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email");
        }
    }
}
