package com.example.movies.specification;

import com.example.movies.model.Course;
import com.example.movies.model.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

public class CourseSpecifications {

    public static Specification<Course> filterCourses(Map<String, String> params) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.containsKey("title")) {
                predicates.add(criteriaBuilder.equal(root.get("title"), params.get("title")));
            }

            if (params.containsKey("title_like")) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + params.get("title_like").toLowerCase() + "%"));
            }

            if (params.containsKey("creditHours")) {
                try {
                    Integer creditHours = Integer.parseInt(params.get("creditHours"));
                    predicates.add(criteriaBuilder.equal(root.get("creditHours"), creditHours));
                } catch (NumberFormatException e) {
                    // Optionally handle or skip
                }
            }

            if (params.containsKey("instructorName")) {
                Join<Course, User> teacherJoin = root.join("teacher", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("username")),
                        "%" + params.get("instructorName").toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
