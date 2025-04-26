package com.example.movies.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendSimpleEmail(String to, String subject, String message) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, false);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailException e) {
            throw new RuntimeException("Не удалось отправить простое письмо на " + to, e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailException e) {
            throw new RuntimeException("Не удалось отправить HTML письмо на " + to, e);
        }
    }

    public void sendEmailWithAttachment(String to, String subject, String message, String filePath) {
        // Пока без реализации
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void sendEmailWithUploadedAttachment(String to, String subject, String message, MultipartFile file) throws MessagingException, IOException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(message, false);
        helper.addAttachment(file.getOriginalFilename(), new ByteArrayResource(file.getBytes()));

        javaMailSender.send(mimeMessage);
    }

    // ✅ Массовая рассылка
    public void sendBulkEmail(List<String> recipients, String subject, String message) {
        for (String to : recipients) {
            try {
                sendSimpleEmail(to, subject, message);
            } catch (RuntimeException e) {
                System.err.println("Ошибка при отправке на: " + to + " — " + e.getMessage());
            }
        }
    }
}
