package com.example.movies.controller;

import com.example.movies.specification.StudentSpecifications;
import com.example.movies.model.Role;
import com.example.movies.model.User;
import com.example.movies.repository.RoleRepository;
import com.example.movies.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/students")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Operation(summary = "Get All Students", description = "Returns all students. Доступен для: ADMIN, TEACHER")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping
    public List<User> getAllStudents() {
        logger.info("Fetching all students");
        try {
            return userRepository.findAll()
                    .stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_USER")))
                    .collect(Collectors.toList());
        } catch(Exception e) {
            logger.error("Error fetching students", e);
            throw e;
        }
    }

    @Operation(summary = "Get Student By ID", description = "Returns student by ID. Доступен для: ADMIN, TEACHER")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/{id}")
    public ResponseEntity<User> getStudentById(@PathVariable Long id) {
        logger.info("Fetching student with id: {}", id);
        try {
            Optional<User> student = userRepository.findById(id);
            return student.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Student id {} not found", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch(Exception e) {
            logger.error("Error fetching student with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Create Student", description = "Creates a new student. Доступен для: ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public User createStudent(@RequestBody User user) {
        logger.info("Creating student: {}", user.getUsername());
        try {
            if(userRepository.findByUsername(user.getUsername()) != null) {
                throw new RuntimeException("Пользователь с таким именем уже существует!");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            Role userRole = roleRepository.findByName("ROLE_USER");
            if(userRole == null) {
                userRole = new Role("ROLE_USER");
                roleRepository.save(userRole);
            }
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);
            return userRepository.save(user);
        } catch(Exception e) {
            logger.error("Error creating student {}: {}", user.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Update Student", description = "Updates student data. Доступен для: ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateStudent(@PathVariable Long id, @RequestBody User userDetails) {
        logger.info("Updating student with id: {}", id);
        try {
            Optional<User> optionalStudent = userRepository.findById(id);
            if(optionalStudent.isPresent()){
                User user = optionalStudent.get();
                user.setUsername(userDetails.getUsername());
                if(userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()){
                    user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                }
                return ResponseEntity.ok(userRepository.save(user));
            }
            logger.warn("Student id {} not found for update", id);
            return ResponseEntity.notFound().build();
        } catch(Exception e) {
            logger.error("Error updating student with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Delete Student", description = "Deletes a student. Доступен для: ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        logger.info("Deleting student with id: {}", id);
        try {
            Optional<User> optionalStudent = userRepository.findById(id);
            if(optionalStudent.isPresent()){
                userRepository.delete(optionalStudent.get());
                return ResponseEntity.ok().build();
            }
            logger.warn("Student id {} not found for deletion", id);
            return ResponseEntity.notFound().build();
        } catch(Exception e) {
            logger.error("Error deleting student with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Filter Students", description = "Returns paginated, sorted, and filtered list of students")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> getFilteredStudents(
            @RequestParam Map<String, String> allParams,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 ? Sort.Direction.fromString(sortParams[1]) : Sort.Direction.ASC;
            Sort.Order order = new Sort.Order(direction, sortParams[0]);
            Pageable pageable = PageRequest.of(page, size, Sort.by(order));

            Specification<User> spec = StudentSpecifications.filterStudents(allParams);
            Page<User> result = userRepository.findAll(spec, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", result.getContent());
            response.put("page", result.getNumber());
            response.put("size", result.getSize());
            response.put("totalElements", result.getTotalElements());
            response.put("totalPages", result.getTotalPages());
            response.put("last", result.isLast());
            response.put("filtersApplied", allParams);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching filtered students: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Filtering failed"));
        }
    }
}
