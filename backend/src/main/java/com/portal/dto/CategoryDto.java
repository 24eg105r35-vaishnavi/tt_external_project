package com.portal.dto;

public class CategoryDto {

    private Long id;
    private String name;
    private String description;
    private int points;
    private boolean active;
    private String verifierDepartment;
    private String fieldSchema;

    public CategoryDto() {
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
