package com.portal.service;

import com.portal.dto.DecisionDto;
import com.portal.entity.Achievement;
import com.portal.entity.AuditLog;
import com.portal.entity.Category;
import com.portal.entity.Decision;
import com.portal.entity.Document;
import com.portal.entity.Faculty;
import com.portal.entity.Notification;
import com.portal.entity.Status;
import com.portal.entity.User;
import com.portal.entity.Verification;
import com.portal.repository.AchievementRepository;
import com.portal.repository.AuditLogRepository;
import com.portal.repository.DocumentRepository;
import com.portal.repository.FacultyRepository;
import com.portal.repository.NotificationRepository;
import com.portal.repository.UserRepository;
import com.portal.repository.VerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationService {

    private final AchievementRepository achievementRepository;
    private final VerificationRepository verificationRepository;
    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationRepository notificationRepository;
    private final DocumentRepository documentRepository;

    public VerificationService(AchievementRepository achievementRepository,
                               VerificationRepository verificationRepository,
                               FacultyRepository facultyRepository,
                               UserRepository userRepository,
                               AuditLogRepository auditLogRepository,
                               NotificationRepository notificationRepository,
                               DocumentRepository documentRepository) {
        this.achievementRepository = achievementRepository;
        this.verificationRepository = verificationRepository;
        this.facultyRepository = facultyRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
        this.documentRepository = documentRepository;
    }

    // helper to find faculty from logged in user email
    public Faculty getFacultyByEmail(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User account not found: " + email));
        return facultyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Faculty profile not found for user: " + email));
    }

    // check if faculty is allowed to review a category
    public boolean isFacultyAllowedForCategory(Faculty faculty, Category category) {
        String verifierDept = category.getVerifierDepartment();
        if (verifierDept == null || verifierDept.trim().isEmpty() || "ALL".equalsIgnoreCase(verifierDept.trim())) {
            return true;
        }
        String fDept = faculty.getDepartment() != null ? faculty.getDepartment().trim().toLowerCase() : "";
        String vDept = verifierDept.trim().toLowerCase();
        if (fDept.equalsIgnoreCase(vDept)) {
            return true;
        }
        if ((fDept.contains("computer") && vDept.equals("cse")) || (fDept.equals("cse") && vDept.contains("computer"))) {
            return true;
        }
        return false;
    }

    // get pending queue for the faculty with filters
    @Transactional(readOnly = true)
    public List<Achievement> getQueueForFaculty(String facultyEmail, String branch, Integer year, Long categoryId) {
        Faculty faculty = getFacultyByEmail(facultyEmail);
        String cleanBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : null;
        return achievementRepository.findPendingForFacultyQueue(faculty.getDepartment(), cleanBranch, year, categoryId);
    }

    // get achievement for review, checking category permission
    @Transactional(readOnly = true)
    public Achievement getAchievementForReview(Long id, String facultyEmail) {
        Faculty faculty = getFacultyByEmail(facultyEmail);
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found with id: " + id));

        if (!isFacultyAllowedForCategory(faculty, achievement.getCategory())) {
            throw new SecurityException("You do not have permission to review achievements in this category.");
        }

        return achievement;
    }

    // get verification history for an achievement
    @Transactional(readOnly = true)
    public List<Verification> getVerificationsForAchievement(Achievement achievement) {
        return verificationRepository.findByAchievementOrderByDecidedAtDesc(achievement);
    }

    // get document for faculty viewing with permission check
    @Transactional(readOnly = true)
    public Document getDocumentForFaculty(Long documentId, String facultyEmail) {
        Faculty faculty = getFacultyByEmail(facultyEmail);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + documentId));

        if (!isFacultyAllowedForCategory(faculty, document.getAchievement().getCategory())) {
            throw new SecurityException("You do not have permission to view documents for this category.");
        }

        return document;
    }

    // decide an achievement all in ONE @Transactional method
    @Transactional
    public void decide(Long achievementId, DecisionDto dto, String facultyEmail) {
        // 1. Find Faculty from logged-in email
        Faculty faculty = getFacultyByEmail(facultyEmail);

        // 2. Load the achievement. It must be PENDING, otherwise throw "Already reviewed"
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found with id: " + achievementId));

        if (achievement.getStatus() != Status.PENDING) {
            throw new IllegalStateException("Already reviewed");
        }

        // 3. Check the faculty is allowed for this category
        if (!isFacultyAllowedForCategory(faculty, achievement.getCategory())) {
            throw new SecurityException("You do not have permission to verify achievements in this category.");
        }

        // 4. Validate decision and required reason
        if (dto.getDecision() == null || dto.getDecision().trim().isEmpty()) {
            throw new IllegalArgumentException("Decision cannot be empty.");
        }

        String d = dto.getDecision().trim().toUpperCase();
        Decision decisionEnum;
        Status newStatus;
        int points;
        String action;
        String notificationMessage;

        if (d.equals("APPROVE") || d.equals("APPROVED")) {
            decisionEnum = Decision.APPROVED;
            newStatus = Status.APPROVED;
            points = achievement.getCategory().getPoints();
            action = "ACHIEVEMENT_APPROVED";
            notificationMessage = "Your achievement '" + achievement.getTitle() + "' was approved! (" + points + " points awarded)";

        } else if (d.equals("REJECT") || d.equals("REJECTED")) {
            if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Reason is required when rejecting an achievement.");
            }
            decisionEnum = Decision.REJECTED;
            newStatus = Status.REJECTED;
            points = 0;
            action = "ACHIEVEMENT_REJECTED";
            notificationMessage = "Your achievement '" + achievement.getTitle() + "' was rejected. Reason: " + dto.getReason().trim();

        } else if (d.equals("SEND_BACK") || d.equals("SENT_BACK") || d.equals("NEEDS_CORRECTION")) {
            if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Reason is required when sending back an achievement for correction.");
            }
            decisionEnum = Decision.SENT_BACK;
            newStatus = Status.NEEDS_CORRECTION;
            points = 0;
            action = "ACHIEVEMENT_SEND_BACK";
            notificationMessage = "Your achievement '" + achievement.getTitle() + "' was sent back for correction. Reason: " + dto.getReason().trim();

        } else {
            throw new IllegalArgumentException("Unknown verification decision: " + dto.getDecision());
        }

        // 5. Update achievement status and pointsAwarded
        Status oldStatus = achievement.getStatus();
        achievement.setStatus(newStatus);
        achievement.setPointsAwarded(points);
        achievementRepository.save(achievement);

        // 6. Insert a NEW Verification row
        Verification verification = new Verification();
        verification.setDecision(decisionEnum);
        verification.setReason(dto.getReason() != null ? dto.getReason().trim() : "");
        verification.setFaculty(faculty);
        verification.setAchievement(achievement);
        verification.setDecidedAt(LocalDateTime.now());
        verificationRepository.save(verification);

        // 7. Insert an AuditLog row
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityName("Achievement");
        auditLog.setEntityId(achievement.getId());
        auditLog.setOldValue(oldStatus.name());
        auditLog.setNewValue(newStatus.name());
        auditLog.setPerformedBy(facultyEmail);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        // 8. Insert a Notification row for the student's user account
        Notification notification = new Notification();
        notification.setMessage(notificationMessage);
        notification.setUser(achievement.getStudent().getUser());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
