package com.example.movies.service;

import com.example.movies.model.Enrollment;
import com.example.movies.repository.EnrollmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public Enrollment enrollStudent(Enrollment enrollment) {
        enrollment.setEnrollmentDate(LocalDate.now());

        Enrollment saved = enrollmentRepository.save(enrollment);

        // Отправка письма
        String to = saved.getStudent().getEmail();
        String subject = "Вы были зачислены на курс";
        String message = "Здравствуйте, " + saved.getStudent().getUsername() +
                "! Вы были успешно зачислены на курс: " +
                saved.getCourse().getTitle();

        emailService.sendSimpleEmail(to, subject, message);

        return saved;
    }
}
