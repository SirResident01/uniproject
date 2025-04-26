package com.example.movies.controller;

import com.example.movies.model.User;
import com.example.movies.model.Role;
import com.example.movies.model.Course;
import com.example.movies.repository.UserRepository;
import com.example.movies.repository.RoleRepository;
import com.example.movies.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@RestController
@RequestMapping("/seed")
public class SeedController {

    private static final Logger logger = LoggerFactory.getLogger(SeedController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping
    public String seedData() {
        logger.info("начало заполнения");
        try {
            Role userRole = roleRepository.findByName("ROLE_USER");
            if (userRole == null) {
                userRole = new Role("ROLE_USER");
                roleRepository.save(userRole);
            }
            Role teacherRole = roleRepository.findByName("ROLE_TEACHER");
            if (teacherRole == null) {
                teacherRole = new Role("ROLE_TEACHER");
                roleRepository.save(teacherRole);
            }
            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = new Role("ROLE_ADMIN");
                roleRepository.save(adminRole);
            }

            for (int i = 1; i <= 4; i++) {
                String username = "user" + i;
                if (userRepository.findByUsername(username) == null) {
                    User u = new User(username, passwordEncoder.encode("123123"));
                    u.setEmail(username + "@example.com");
                    u.setRoles(new HashSet<>(Arrays.asList(userRole)));
                    userRepository.save(u);
                }
            }

            for (int i = 1; i <= 4; i++) {
                String username = "teacher" + i;
                if (userRepository.findByUsername(username) == null) {
                    User u = new User(username, passwordEncoder.encode("123123"));
                    u.setEmail(username + "@example.com");
                    u.setRoles(new HashSet<>(Arrays.asList(teacherRole)));
                    userRepository.save(u);
                }
            }

            for (int i = 1; i <= 4; i++) {
                String username = "admin" + i;
                if (userRepository.findByUsername(username) == null) {
                    User u = new User(username, passwordEncoder.encode("123123"));
                    u.setEmail(username + "@example.com");
                    u.setRoles(new HashSet<>(Arrays.asList(adminRole)));
                    userRepository.save(u);
                }
            }

            for (int i = 1; i <= 4; i++) {
                Course course = new Course();
                course.setTitle("Course " + i);
                course.setDescription("Description for course " + i);
                courseRepository.save(course);
            }

            logger.info("заполнение завершено");
            return "completed";
        } catch (Exception e) {
            logger.error("Error during seeding", e);
            return "failed: " + e.getMessage();
        }
    }
}
