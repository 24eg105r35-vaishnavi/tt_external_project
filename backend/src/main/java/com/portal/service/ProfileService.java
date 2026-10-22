package com.portal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dto.PublicAchievementDto;
import com.portal.dto.PublicProfileDto;
import com.portal.entity.Achievement;
import com.portal.entity.AuditLog;
import com.portal.entity.Decision;
import com.portal.entity.Status;
import com.portal.entity.Student;
import com.portal.entity.User;
import com.portal.entity.Verification;
import com.portal.entity.Visibility;
import com.portal.repository.AchievementRepository;
import com.portal.repository.AuditLogRepository;
import com.portal.repository.StudentRepository;
import com.portal.repository.UserRepository;
import com.portal.repository.VerificationRepository;
import com.portal.util.ProfileNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfileService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final VerificationRepository verificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public ProfileService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          AchievementRepository achievementRepository,
                          VerificationRepository verificationRepository,
                          AuditLogRepository auditLogRepository,
                          ObjectMapper objectMapper) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.verificationRepository = verificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    // load student public profile adhering to visibility rules
    @Transactional(readOnly = true)
    public PublicProfileDto getPublicProfile(String slug, String viewerEmail) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new ProfileNotFoundException("Profile not found");
        }

        Student student = studentRepository.findByProfileSlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));

        Visibility visibility = student.getVisibility();

        // 1. check visibility rule
        if (visibility == Visibility.PUBLIC) {
            // anyone can view, even not logged in
        } else if (visibility == Visibility.COLLEGE_ONLY) {
            // only logged in user can view
            if (viewerEmail == null || viewerEmail.trim().isEmpty() || "anonymousUser".equalsIgnoreCase(viewerEmail.trim())) {
                throw new ProfileNotFoundException("Profile not found");
            }
        } else if (visibility == Visibility.PRIVATE) {
            // only the owner can view
            if (viewerEmail == null || student.getUser() == null || !viewerEmail.trim().equalsIgnoreCase(student.getUser().getEmail().trim())) {
                throw new ProfileNotFoundException("Profile not found");
            }
        }

        // 2. load only APPROVED achievements ordered by eventDate desc
        List<Achievement> approvedAchievements = achievementRepository.findApprovedAchievementsForProfile(student, Status.APPROVED);

        int totalPoints = 0;
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        Map<String, List<PublicAchievementDto>> groupedAchievements = new LinkedHashMap<>();

        for (Achievement ach : approvedAchievements) {
            totalPoints += ach.getPointsAwarded();

            String catName = ach.getCategory().getName();
            categoryCounts.put(catName, categoryCounts.getOrDefault(catName, 0) + 1);

            // parse extraData JSON
            Map<String, Object> extraFieldsMap = null;
            if (ach.getExtraData() != null && !ach.getExtraData().trim().isEmpty()) {
                try {
                    extraFieldsMap = objectMapper.readValue(ach.getExtraData(), new TypeReference<Map<String, Object>>() {});
                } catch (Exception ignored) {}
            }

            // find latest approved verification date
            LocalDateTime verifiedAt = null;
            List<Verification> verifications = verificationRepository.findByAchievementOrderByDecidedAtDesc(ach);
            for (Verification v : verifications) {
                if (v.getDecision() == Decision.APPROVED) {
                    verifiedAt = v.getDecidedAt();
                    break;
                }
            }

            PublicAchievementDto item = new PublicAchievementDto();
            item.setId(ach.getId());
            item.setTitle(ach.getTitle());
            item.setCategoryName(catName);
            item.setLevel(ach.getLevel());
            item.setEventDate(ach.getEventDate());
            item.setOrganizer(ach.getOrganizer());
            item.setPointsAwarded(ach.getPointsAwarded());
            item.setExtraFields(extraFieldsMap);
            item.setVerifiedAt(verifiedAt);

            if (!groupedAchievements.containsKey(catName)) {
                groupedAchievements.put(catName, new ArrayList<>());
            }
            groupedAchievements.get(catName).add(item);
        }

        PublicProfileDto dto = new PublicProfileDto();
        dto.setFullName(student.getUser().getFullName());
        dto.setBranch(student.getBranch());
        dto.setYear(student.getYear());
        dto.setTotalPoints(totalPoints);
        dto.setVisibility(student.getVisibility());
        dto.setCategoryCounts(categoryCounts);
        dto.setGroupedAchievements(groupedAchievements);

        return dto;
    }

    // get student details for logged-in student settings page
    @Transactional(readOnly = true)
    public Student getStudentProfile(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        return studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for user: " + email));
    }

    // update profile visibility with AuditLog
    @Transactional
    public void updateVisibility(String email, Visibility newVisibility) {
        Student student = getStudentProfile(email);
        Visibility oldVisibility = student.getVisibility();

        student.setVisibility(newVisibility);
        studentRepository.save(student);

        // write AuditLog row
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("PROFILE_VISIBILITY_CHANGED");
        auditLog.setEntityName("Student");
        auditLog.setEntityId(student.getId());
        auditLog.setOldValue(oldVisibility != null ? oldVisibility.name() : "PRIVATE");
        auditLog.setNewValue(newVisibility.name());
        auditLog.setPerformedBy(email);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }
}
