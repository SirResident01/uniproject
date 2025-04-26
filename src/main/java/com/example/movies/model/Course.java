package com.example.movies.model;

import com.example.movies.JsonViews;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(JsonViews.Public.class)
    private Long id;

    @JsonView(JsonViews.Public.class)
    private String title;

    @JsonView(JsonViews.Public.class)
    private String description;

    @JsonView(JsonViews.Public.class)
    private Integer creditHours; // 👈 добавлено новое поле

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @JsonView(JsonViews.Public.class)
    private User teacher;

    @ManyToMany
    @JoinTable(
            name = "course_student",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @JsonView(JsonViews.Internal.class)
    private Set<User> students = new HashSet<>();

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(Integer creditHours) {
        this.creditHours = creditHours;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public Set<User> getStudents() {
        return students;
    }

    public void setStudents(Set<User> students) {
        this.students = students;
    }
}
