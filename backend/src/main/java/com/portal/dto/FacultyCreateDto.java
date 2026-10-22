package com.portal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO for admin creating a new faculty account
@Getter
@Setter
@NoArgsConstructor
public class FacultyCreateDto {

    private String fullName;
    private String email;
    private String department;
    private String designation;
    private String password;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
