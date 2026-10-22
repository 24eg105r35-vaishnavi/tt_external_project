package com.portal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dto.AchievementDto;
import com.portal.dto.StoredFileInfo;
import com.portal.entity.Achievement;
import com.portal.entity.AuditLog;
import com.portal.entity.Category;
import com.portal.entity.Document;
import com.portal.entity.Status;
import com.portal.entity.Student;
import com.portal.entity.User;
import com.portal.repository.AchievementRepository;
import com.portal.repository.AuditLogRepository;
import com.portal.repository.CategoryRepository;
import com.portal.repository.DocumentRepository;
import com.portal.repository.StudentRepository;
import com.portal.repository.UserRepository;
import com.portal.util.FileStorageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final CategoryRepository categoryRepository;
    private final DocumentRepository documentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    public AchievementService(AchievementRepository achievementRepository,
                              CategoryRepository categoryRepository,
                              DocumentRepository documentRepository,
                              StudentRepository studentRepository,
                              UserRepository userRepository,
                              AuditLogRepository auditLogRepository,
                              FileStorageService fileStorageService,
                              ObjectMapper objectMapper) {
        this.achievementRepository = achievementRepository;
        this.categoryRepository = categoryRepository;
        this.documentRepository = documentRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    // submit achievement with validation, file saving, and audit log in one transaction
    @Transactional
    public Achievement submitAchievement(AchievementDto dto,
                                         MultipartFile file,
                                         Map<String, String> extraFields,
                                         String userEmail) {
        // 1. find the student from the logged in user's email
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User account not found: " + userEmail));

        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for user: " + userEmail));

        // 2. load the category (must be active)
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Selected category does not exist."));

        if (!category.isActive()) {
            throw new IllegalArgumentException("Selected category is currently inactive.");
        }

        // 3. validate dynamic extra fields against category's fieldSchema
        Map<String, Object> validatedExtraData = validateAndCleanExtraFields(category.getFieldSchema(), extraFields);
        String extraDataJson = null;
        try {
            extraDataJson = objectMapper.writeValueAsString(validatedExtraData);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to format extra fields data.");
        }

        // 4. store proof file on disk and compute checksum
        StoredFileInfo fileInfo = fileStorageService.storeFile(file, student);

        try {
            // 5. save Achievement entity with status PENDING and pointsAwarded 0
            Achievement achievement = new Achievement();
            achievement.setTitle(dto.getTitle().trim());
            achievement.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
            achievement.setLevel(dto.getLevel().trim());
            achievement.setEventDate(dto.getEventDate());
            achievement.setOrganizer(dto.getOrganizer().trim());
            achievement.setExtraData(extraDataJson);
            achievement.setStatus(Status.PENDING);
            achievement.setPointsAwarded(0);
            achievement.setStudent(student);
            achievement.setCategory(category);
            achievement.setCreatedAt(LocalDateTime.now());
            Achievement savedAchievement = achievementRepository.save(achievement);

            // 6. save Document entity linked to achievement
            Document document = new Document();
            document.setOriginalName(fileInfo.getOriginalName());
            document.setStoredName(fileInfo.getStoredName());
            document.setFilePath(fileInfo.getFilePath());
            document.setFileType(fileInfo.getFileType());
            document.setFileSize(fileInfo.getFileSize());
            document.setChecksum(fileInfo.getChecksum());
            document.setUploadedAt(LocalDateTime.now());
            document.setAchievement(savedAchievement);
            documentRepository.save(document);

            // 7. add AuditLog row
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("ACHIEVEMENT_SUBMITTED");
            auditLog.setEntityName("Achievement");
            auditLog.setEntityId(savedAchievement.getId());
            auditLog.setOldValue(null);
            auditLog.setNewValue("Title: " + savedAchievement.getTitle() + ", Category: " + category.getName());
            auditLog.setPerformedBy(userEmail);
            auditLog.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(auditLog);

            return savedAchievement;

        } catch (Exception e) {
            // if database save fails, delete file from disk and rethrow to trigger rollback
            fileStorageService.deleteFile(fileInfo.getFilePath());
            throw e;
        }
    }

    // get list of achievements belonging to the student
    @Transactional(readOnly = true)
    public List<Achievement> getAchievementsForStudent(String userEmail) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Student not found for user: " + userEmail));
        return achievementRepository.findByStudentOrderByCreatedAtDesc(student);
    }

    // get single achievement and enforce ownership check
    @Transactional(readOnly = true)
    public Achievement getAchievementById(Long id, String userEmail) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found with id: " + id));

        // refuse if achievement belongs to another student
        String ownerEmail = achievement.getStudent().getUser().getEmail();
        if (!ownerEmail.equalsIgnoreCase(userEmail.trim())) {
            throw new SecurityException("You do not have permission to view this achievement.");
        }

        return achievement;
    }

    // get document and enforce ownership check
    @Transactional(readOnly = true)
    public Document getDocumentById(Long id, String userEmail) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + id));

        // refuse if document's achievement belongs to another student
        String ownerEmail = document.getAchievement().getStudent().getUser().getEmail();
        if (!ownerEmail.equalsIgnoreCase(userEmail.trim())) {
            throw new SecurityException("You do not have permission to view this document.");
        }

        return document;
    }

    // helper to get documents for an achievement
    @Transactional(readOnly = true)
    public List<Document> getDocumentsForAchievement(Achievement achievement) {
        return documentRepository.findByAchievement(achievement);
    }

    // helper to validate dynamic fields against schema
    private Map<String, Object> validateAndCleanExtraFields(String schemaJson, Map<String, String> submittedFields) {
        Map<String, Object> cleanData = new HashMap<>();

        if (schemaJson == null || schemaJson.trim().isEmpty() || !schemaJson.trim().startsWith("[")) {
            return cleanData;
        }

        try {
            List<Map<String, Object>> schemaList = objectMapper.readValue(schemaJson, new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> fieldDef : schemaList) {
                String fieldName = (String) fieldDef.get("name");
                String label = (String) fieldDef.getOrDefault("label", fieldName);
                String type = (String) fieldDef.getOrDefault("type", "text");
                Boolean required = (Boolean) fieldDef.getOrDefault("required", false);

                String submittedValue = submittedFields != null ? submittedFields.get(fieldName) : null;
                if (submittedValue != null) {
                    submittedValue = submittedValue.trim();
                }

                // check required
                if (Boolean.TRUE.equals(required) && (submittedValue == null || submittedValue.isEmpty())) {
                    throw new IllegalArgumentException(label + " is required.");
                }

                if (submittedValue != null && !submittedValue.isEmpty()) {
                    if ("number".equalsIgnoreCase(type)) {
                        try {
                            // parse number
                            if (submittedValue.contains(".")) {
                                cleanData.put(fieldName, Double.parseDouble(submittedValue));
                            } else {
                                cleanData.put(fieldName, Long.parseLong(submittedValue));
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(label + " must be a valid number.");
                        }
                    } else if ("select".equalsIgnoreCase(type)) {
                        @SuppressWarnings("unchecked")
                        List<String> options = (List<String>) fieldDef.get("options");
                        if (options != null && !options.contains(submittedValue)) {
                            throw new IllegalArgumentException(label + " contains an invalid option.");
                        }
                        cleanData.put(fieldName, submittedValue);
                    } else {
                        cleanData.put(fieldName, submittedValue);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid category field schema definition.");
        }

        return cleanData;
    }

    // resubmit achievement when in REJECTED or NEEDS_CORRECTION status
    @Transactional
    public Achievement resubmitAchievement(Long achievementId,
                                          AchievementDto dto,
                                          MultipartFile file,
                                          Map<String, String> extraFields,
                                          String userEmail) {
        // 1. load achievement and check student ownership
        Achievement achievement = getAchievementById(achievementId, userEmail);

        // 2. check status: only REJECTED or NEEDS_CORRECTION can be edited
        if (achievement.getStatus() != Status.REJECTED && achievement.getStatus() != Status.NEEDS_CORRECTION) {
            throw new IllegalStateException("Cannot edit achievement in " + achievement.getStatus() + " status. Only REJECTED or NEEDS_CORRECTION items can be edited.");
        }

        // 3. update standard fields
        achievement.setTitle(dto.getTitle().trim());
        achievement.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        achievement.setLevel(dto.getLevel().trim());
        achievement.setEventDate(dto.getEventDate());
        achievement.setOrganizer(dto.getOrganizer().trim());

        // 4. validate dynamic extra fields
        Map<String, Object> validatedExtraData = validateAndCleanExtraFields(achievement.getCategory().getFieldSchema(), extraFields);
        try {
            achievement.setExtraData(objectMapper.writeValueAsString(validatedExtraData));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to format extra fields data.");
        }

        // 5. optional new proof document upload
        if (file != null && !file.isEmpty()) {
            StoredFileInfo fileInfo = fileStorageService.storeFile(file, achievement.getStudent());
            Document document = new Document();
            document.setOriginalName(fileInfo.getOriginalName());
            document.setStoredName(fileInfo.getStoredName());
            document.setFilePath(fileInfo.getFilePath());
            document.setFileType(fileInfo.getFileType());
            document.setFileSize(fileInfo.getFileSize());
            document.setChecksum(fileInfo.getChecksum());
            document.setUploadedAt(LocalDateTime.now());
            document.setAchievement(achievement);
            documentRepository.save(document);
        }

        // 6. reset status to PENDING and pointsAwarded to 0
        Status oldStatus = achievement.getStatus();
        achievement.setStatus(Status.PENDING);
        achievement.setPointsAwarded(0);
        Achievement savedAchievement = achievementRepository.save(achievement);

        // 7. create AuditLog row (old Verification rows stay untouched)
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("ACHIEVEMENT_RESUBMITTED");
        auditLog.setEntityName("Achievement");
        auditLog.setEntityId(savedAchievement.getId());
        auditLog.setOldValue(oldStatus.name());
        auditLog.setNewValue(Status.PENDING.name());
        auditLog.setPerformedBy(userEmail);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return savedAchievement;
    }

    // student dashboard stats: counts, total points, and recent 5 achievements
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentDashboardStats(String userEmail) {
        List<Achievement> achievements = getAchievementsForStudent(userEmail);

        int pendingCount = 0;
        int approvedCount = 0;
        int needsActionCount = 0;
        int totalPoints = 0;

        for (Achievement a : achievements) {
            if (a.getStatus() == Status.PENDING) {
                pendingCount++;
            } else if (a.getStatus() == Status.APPROVED) {
                approvedCount++;
                totalPoints += a.getPointsAwarded();
            } else if (a.getStatus() == Status.NEEDS_CORRECTION || a.getStatus() == Status.REJECTED) {
                needsActionCount++;
            }
        }

        // extract 5 most recent achievements
        List<Achievement> recent = new ArrayList<>();
        for (int i = 0; i < achievements.size() && i < 5; i++) {
            recent.add(achievements.get(i));
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", pendingCount);
        stats.put("approvedCount", approvedCount);
        stats.put("needsActionCount", needsActionCount);
        stats.put("totalPoints", totalPoints);
        stats.put("recentAchievements", recent);
        return stats;
    }
}
