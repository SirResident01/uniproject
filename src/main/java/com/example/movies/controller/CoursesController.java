package com.example.movies.controller;

import com.example.movies.JsonViews;
import com.example.movies.model.Course;
import com.example.movies.model.User;
import com.example.movies.repository.CourseRepository;
import com.example.movies.repository.UserRepository;
import com.example.movies.specification.CourseSpecifications;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/courses")
public class CoursesController {

    private static final Logger logger = LoggerFactory.getLogger(CoursesController.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Get All Courses", description = "Returns all courses. Accessible for: ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(JsonViews.Public.class)
    @GetMapping
    public List<Course> getAllCourses() {
        logger.info("Fetching all courses");
        return courseRepository.findAll();
    }

    @Operation(summary = "Get Course By ID", description = "Returns course by ID. Accessible for: ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(JsonViews.Public.class)
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        logger.info("Fetching course with id: {}", id);
        return courseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create Course", description = "Creates a course. Accessible for: ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        logger.info("Creating course: {}", course.getTitle());
        return courseRepository.save(course);
    }

    @Operation(summary = "Update Course", description = "Updates a course. Accessible for: ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        logger.info("Updating course with id: {}", id);
        return courseRepository.findById(id)
                .map(course -> {
                    course.setCreditHours(courseDetails.getCreditHours());
                    course.setTitle(courseDetails.getTitle());
                    course.setTeacher(courseDetails.getTeacher());
                    course.setDescription(courseDetails.getDescription());
                    return ResponseEntity.ok(courseRepository.save(course));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete Course", description = "Deletes a course. Accessible for: ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        logger.info("Deleting course with id: {}", id);
        return courseRepository.findById(id)
                .map(course -> {
                    courseRepository.delete(course);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Assign Teacher", description = "Assigns a teacher to a course. Accessible for: TEACHER")
    @PreAuthorize("hasRole('TEACHER')")
    @JsonView(JsonViews.Public.class)
    @PostMapping("/{id}/assign-teacher")
    public ResponseEntity<Course> assignTeacher(@PathVariable Long id, @RequestParam Long teacherId) {
        Course course = courseRepository.findById(id).orElse(null);
        User teacher = userRepository.findById(teacherId).orElse(null);
        if (course == null || teacher == null) return ResponseEntity.badRequest().build();

        course.setTeacher(teacher);
        return ResponseEntity.ok(courseRepository.save(course));
    }

    @Operation(summary = "Get Paginated Courses", description = "Returns paginated and sorted list of courses. Format: sort=title,asc")
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(JsonViews.Public.class)
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getPaginatedCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title,asc") String sort) {

        try {
            String[] sortParts = sort.split(",");
            Sort.Order order = new Sort.Order(Sort.Direction.fromString(sortParts[1]), sortParts[0]);
            Pageable pageable = PageRequest.of(page, size, Sort.by(order));
            Page<Course> coursePage = courseRepository.findAll(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", coursePage.getContent());
            response.put("page", coursePage.getNumber());
            response.put("size", coursePage.getSize());
            response.put("totalElements", coursePage.getTotalElements());
            response.put("totalPages", coursePage.getTotalPages());
            response.put("last", coursePage.isLast());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching paginated courses: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Invalid pagination or sorting parameters"));
        }
    }

    @Operation(summary = "Filter Courses", description = "Filter by title, title_like, instructorName. Accessible for: ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterCourses(
            @RequestParam Map<String, String> allParams,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort // теперь строка, не массив
    ) {
        try {
            // сортировка по одному полю
            String[] sortParams = sort.split(",");
            Sort.Order order = new Sort.Order(Sort.Direction.fromString(sortParams[1]), sortParams[0]);

            Pageable pageable = PageRequest.of(page, size, Sort.by(order));
            Specification<Course> spec = CourseSpecifications.filterCourses(allParams);

            Page<Course> result = courseRepository.findAll(spec, pageable);

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
            return ResponseEntity.internalServerError().body(Map.of("error", "Filtering failed"));
        }
    }
}
