package com.portal.dto;

import java.time.LocalDateTime;

public class VerificationDto {

    private Long id;
    private String decision;
    private String reason;
    private LocalDateTime decidedAt;
    private String verifierName;
    private String verifierDepartment;

    public VerificationDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public String getVerifierName() {
        return verifierName;
    }

    public void setVerifierName(String verifierName) {
        this.verifierName = verifierName;
    }

    public String getVerifierDepartment() {
        return verifierDepartment;
    }

    public void setVerifierDepartment(String verifierDepartment) {
        this.verifierDepartment = verifierDepartment;
    }
}
