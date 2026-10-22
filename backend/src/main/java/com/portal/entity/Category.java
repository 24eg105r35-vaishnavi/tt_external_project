package com.portal.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private int points;

    private boolean active = true;

    private String verifierDepartment;

    // simple JSON string storing extra field requirements
    @Column(columnDefinition = "TEXT")
    private String fieldSchema;

    // Default no-argument constructor required by Hibernate/JPA
    public Category() {
    }

    public Category(String name, String description, int points, String verifierDepartment, String fieldSchema) {
        this.name = name;
        this.description = description;
        this.points = points;
        this.verifierDepartment = verifierDepartment;
        this.fieldSchema = fieldSchema;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getVerifierDepartment() {
        return verifierDepartment;
    }

    public void setVerifierDepartment(String verifierDepartment) {
        this.verifierDepartment = verifierDepartment;
    }

    public String getFieldSchema() {
        return fieldSchema;
    }

    public void setFieldSchema(String fieldSchema) {
        this.fieldSchema = fieldSchema;
    }
}
