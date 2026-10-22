package com.portal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AchievementResponseDto {

    private Long id;
    private String title;
    private String description;
    private String level;
    private LocalDate eventDate;
    private String organizer;
    private String extraData;
    private String status;
    private int pointsAwarded;
    private LocalDateTime createdAt;

    // student summary
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String branch;
    private int year;

    // category summary
    private Long categoryId;
    private String categoryName;

    // document metadata
    private Long documentId;
    private String documentOriginalName;
    private String documentFileType;
    private long documentFileSize;

    // verification timeline
    private List<VerificationDto> verifications = new ArrayList<>();

    public AchievementResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(int pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocumentOriginalName() {
        return documentOriginalName;
    }

    public void setDocumentOriginalName(String documentOriginalName) {
        this.documentOriginalName = documentOriginalName;
    }

    public String getDocumentFileType() {
        return documentFileType;
    }

    public void setDocumentFileType(String documentFileType) {
        this.documentFileType = documentFileType;
    }

    public long getDocumentFileSize() {
        return documentFileSize;
    }

    public void setDocumentFileSize(long documentFileSize) {
        this.documentFileSize = documentFileSize;
    }

    public List<VerificationDto> getVerifications() {
        return verifications;
    }

    public void setVerifications(List<VerificationDto> verifications) {
        this.verifications = verifications;
    }
}
