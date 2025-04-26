package com.example.movies.model;

import com.example.movies.JsonViews;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(JsonViews.Public.class)
    private Long id;

    @Column(unique = true)
    @JsonView(JsonViews.Public.class)
    private String name;

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    // getters/setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
