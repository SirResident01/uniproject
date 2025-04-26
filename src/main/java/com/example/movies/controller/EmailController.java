package com.example.movies.controller;

import com.example.movies.model.User;
import com.example.movies.repository.UserRepository;
import com.example.movies.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Send Simple Email", description = "Отправка простого текстового письма. Доступен ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/text")
    public ResponseEntity<?> sendTextEmail(@RequestParam String to,
                                           @RequestParam String subject,
                                           @RequestParam String message) {
        emailService.sendSimpleEmail(to, subject, message);
        return ResponseEntity.ok("Текстовое письмо отправлено!");
    }

    @Operation(summary = "Send HTML Email", description = "Отправка HTML письма. Доступен ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/html")
    public ResponseEntity<?> sendHtmlEmail(@RequestParam String to,
                                           @RequestParam String subject,
                                           @RequestParam String htmlContent) {
        emailService.sendHtmlEmail(to, subject, htmlContent);
        return ResponseEntity.ok("HTML письмо отправлено!");
    }

    @Operation(summary = "Send Email with Uploaded Attachment", description = "Отправка письма с вложенным файлом (загрузка через Swagger). Доступен ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/upload-attachment", consumes = "multipart/form-data")
    public ResponseEntity<?> sendEmailWithUpload(@RequestParam String to,
                                                 @RequestParam String subject,
                                                 @RequestParam String message,
                                                 @RequestPart MultipartFile file) {
        try {
            emailService.sendEmailWithUploadedAttachment(to, subject, message, file);
            return ResponseEntity.ok("Письмо с загруженным вложением отправлено!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при отправке письма: " + e.getMessage());
        }
    }

    @Operation(summary = "Send Bulk Emails", description = "Массовая рассылка писем на выбранные адреса. Доступен ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk")
    public ResponseEntity<?> sendBulkEmails(@RequestParam List<@Email String> recipients,
                                            @RequestParam String subject,
                                            @RequestParam String message) {
        emailService.sendBulkEmail(recipients, subject, message);
        return ResponseEntity.ok("Массовая рассылка выполнена!");
    }

    @Operation(summary = "Send Email by Role", description = "Автоматическая рассылка по роли: STUDENT, TEACHER, ADMIN или ALL. Доступен ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/send-to-all")
    public ResponseEntity<?> sendToAllUsers(@RequestParam String subject,
                                            @RequestParam String message,
                                            @RequestParam(defaultValue = "ALL") String role) {

        List<String> emails = userRepository.findAll().stream()
                .filter(user -> user.getEmail() != null && !user.getEmail().isBlank())
                .filter(user -> {
                    if (role.equalsIgnoreCase("ALL")) return true;
                    return user.getRoles().stream()
                            .anyMatch(r -> r.getName().equalsIgnoreCase("ROLE_" + role));
                })
                .map(User::getEmail)
                .collect(Collectors.toList());

        if (emails.isEmpty()) {
            return ResponseEntity.badRequest().body("Нет email-адресов для рассылки по роли: " + role);
        }

        emailService.sendBulkEmail(emails, subject, message);
        return ResponseEntity.ok("Письма отправлены " + emails.size() + " пользователям с ролью " + role.toUpperCase());
    }
}
