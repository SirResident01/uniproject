package com.example.movies.model;

import com.example.movies.JsonViews;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(JsonViews.Public.class)
    private Long id;

    @Column(unique = true)
    @JsonView(JsonViews.Public.class)
    private String username;

    @JsonIgnore // 👈 Всегда скрываем пароль
    private String password;

    @JsonIgnore // 👈 Скрываем роли, если они не нужны в API. Можно заменить на @JsonView(JsonViews.Internal.class) при необходимости
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore // 👈 Скрываем курсы, в которых пользователь студент
    @ManyToMany(mappedBy = "students")
    private Set<Course> coursesEnrolled = new HashSet<>();

    @JsonIgnore // 👈 Скрываем курсы, которые пользователь преподаёт
    @OneToMany(mappedBy = "teacher")
    private Set<Course> coursesTaught = new HashSet<>();

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Course> getCoursesEnrolled() {
        return coursesEnrolled;
    }

    public void setCoursesEnrolled(Set<Course> coursesEnrolled) {
        this.coursesEnrolled = coursesEnrolled;
    }

    public Set<Course> getCoursesTaught() {
        return coursesTaught;
    }

    public void setCoursesTaught(Set<Course> coursesTaught) {
        this.coursesTaught = coursesTaught;
    }
    @Column(nullable = false, unique = true)
    @JsonView(JsonViews.Public.class)
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
