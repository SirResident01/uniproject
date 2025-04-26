package com.example.movies.repository;

import com.example.movies.model.Enrollment;
import com.example.movies.model.User;
import com.example.movies.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, JpaSpecificationExecutor<Enrollment> {
    boolean existsByStudentAndCourse(User student, Course course); // ✅ этот метод нужен
}
