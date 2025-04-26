package com.example.movies.controller;

import com.example.movies.model.Enrollment;
import com.example.movies.model.User;
import com.example.movies.model.Course;
import com.example.movies.repository.EnrollmentRepository;
import com.example.movies.repository.UserRepository;
import com.example.movies.repository.CourseRepository;
import com.example.movies.service.EmailService;
import com.example.movies.specification.EnrollmentSpecifications;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentsController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EmailService emailService;

    @Operation(summary = "Enroll student to course", description = "Enrolls a student to a course and sends email. Accessible for: USER or ADMIN")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/enroll")
    public ResponseEntity<?> enrollStudentByParams(@RequestParam Long studentId,
                                                   @RequestParam Long courseId) {
        try {
            User student = userRepository.findById(studentId).orElse(null);
            Course course = courseRepository.findById(courseId).orElse(null);

            if (student == null || course == null) {
                return ResponseEntity.badRequest().body("Некорректный studentId или courseId");
            }

            // 🔒 Проверка: нельзя зачислить TEACHER или ADMIN
            boolean isTeacherOrAdmin = student.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ROLE_TEACHER") || role.getName().equals("ROLE_ADMIN"));
            if (isTeacherOrAdmin) {
                return ResponseEntity.badRequest().body("Преподаватели и админы не могут быть зачислены на курсы.");
            }

            // 🔒 Проверка: уже зачислен
            boolean alreadyEnrolled = enrollmentRepository.existsByStudentAndCourse(student, course);
            if (alreadyEnrolled) {
                return ResponseEntity.badRequest().body("Студент уже зачислен на этот курс.");
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollment.setEnrollmentDate(LocalDate.now());

            Enrollment saved = enrollmentRepository.save(enrollment);

            // 📩 Email
            String to = student.getEmail();
            if (to != null && !to.isBlank()) {
                String subject = "Зачисление на курс";
                String message = String.format("Вы были зачислены на курс: %s", course.getTitle());
                emailService.sendSimpleEmail(to, subject, message);
            }

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при зачислении: " + e.getMessage());
        }
    }

    @Operation(summary = "Unenroll student from course", description = "Removes a student's enrollment. Accessible for: USER or ADMIN")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> unenroll(@PathVariable Long id) {
        if (enrollmentRepository.existsById(id)) {
            enrollmentRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get all enrollments", description = "Returns all enrollments. Accessible for: ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentRepository.findAll());
    }

    @Operation(summary = "Get paginated enrollments with filtering", description = "Returns enrollments with filters: studentId, courseId, enrollmentDate")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> getFilteredEnrollments(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String enrollmentDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        try {
            LocalDate parsedDate = enrollmentDate != null ? LocalDate.parse(enrollmentDate) : null;

            Specification<Enrollment> spec = EnrollmentSpecifications.filterEnrollments(studentId, courseId, parsedDate);

            String[] sortParams = sort.split(",");
            Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
            Sort.Order order = new Sort.Order(direction, sortParams[0]);
            Pageable pageable = PageRequest.of(page, size, Sort.by(order));

            Page<Enrollment> result = enrollmentRepository.findAll(spec, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", result.getContent());
            response.put("page", result.getNumber());
            response.put("size", result.getSize());
            response.put("totalElements", result.getTotalElements());
            response.put("totalPages", result.getTotalPages());
            response.put("last", result.isLast());

            Map<String, Object> filters = new HashMap<>();
            if (studentId != null) filters.put("studentId", studentId);
            if (courseId != null) filters.put("courseId", courseId);
            if (enrollmentDate != null) filters.put("enrollmentDate", enrollmentDate);
            response.put("filtersApplied", filters);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Filtering failed"));
        }
    }
}
