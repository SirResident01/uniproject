
package com.example.movies.specification;

import com.example.movies.model.Course;
import com.example.movies.model.User;
import jakarta.persistence.criteria.Join;
import com.example.movies.model.Enrollment;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentSpecifications {

    public static Specification<Enrollment> filterEnrollments(Long studentId, Long courseId, LocalDate enrollmentDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (studentId != null) {
                Join<Enrollment, User> studentJoin = root.join("student");
                predicates.add(criteriaBuilder.equal(studentJoin.get("id"), studentId));
            }

            if (courseId != null) {
                Join<Enrollment, Course> courseJoin = root.join("course");
                predicates.add(criteriaBuilder.equal(courseJoin.get("id"), courseId));
            }

            if (enrollmentDate != null) {
                predicates.add(criteriaBuilder.equal(root.get("enrollmentDate"), enrollmentDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
