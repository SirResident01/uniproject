package com.example.movies.specification;

import com.example.movies.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

public class StudentSpecifications {

    public static Specification<User> filterStudents(Map<String, String> params) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (params.containsKey("name")) {
                String name = params.get("name");
                if (name != null && !name.isBlank()) {
                    predicate = cb.and(predicate, cb.equal(cb.lower(root.get("username")), name.toLowerCase()));
                }
            }

            if (params.containsKey("email")) {
                String email = params.get("email");
                if (email != null && !email.isBlank()) {
                    predicate = cb.and(predicate, cb.equal(cb.lower(root.get("email")), email.toLowerCase()));
                }
            }

            if (params.containsKey("group")) {
                String group = params.get("group");
                if (group != null && !group.isBlank()) {
                    predicate = cb.and(predicate, cb.equal(cb.lower(root.get("group")), group.toLowerCase()));
                }
            }

            if (params.containsKey("name_like")) {
                String nameLike = params.get("name_like");
                if (nameLike != null && !nameLike.isBlank()) {
                    predicate = cb.and(predicate, cb.like(cb.lower(root.get("username")), nameLike.toLowerCase() + "%"));
                }
            }

            return predicate;
        };
    }
}
