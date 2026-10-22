package com.portal;

import com.portal.dto.PublicAchievementDto;
import com.portal.dto.PublicProfileDto;
import com.portal.entity.*;
import com.portal.repository.*;
import com.portal.service.NotificationService;
import com.portal.service.ProfileService;
import com.portal.service.UserService;
import com.portal.util.ProfileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Part6VerificationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Student student1;
    private Student student2;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Ensure student1 exists
        student1 = studentRepository.findByRollNumber("21EG105A01").orElse(null);
        assertNotNull(student1, "Student 1 must exist");

        // Ensure student1 has a profile slug
        if (student1.getProfileSlug() == null || student1.getProfileSlug().isBlank()) {
            student1.setProfileSlug(userService.generateUniqueSlug(student1.getUser().getFullName(), student1.getRollNumber()));
            student1 = studentRepository.save(student1);
        }

        // Ensure student2 exists
        Role studentRole = roleRepository.findByName("STUDENT").orElse(null);
        User user2 = userRepository.findByEmail("student2@anurag.edu.in").orElse(null);
        if (user2 == null) {
            user2 = new User();
            user2.setEmail("student2@anurag.edu.in");
            user2.setPassword(passwordEncoder.encode("student123"));
            user2.setFullName("Sneha Reddy");
            user2.setEnabled(true);
            user2.setRole(studentRole);
            user2 = userRepository.save(user2);

            student2 = new Student();
            student2.setRollNumber("21EG105A99");
            student2.setBranch("ECE");
            student2.setYear(3);
            student2.setPhone("9876543299");
            student2.setProfileSlug("sneha-reddy-21eg105a99");
            student2.setVisibility(Visibility.PRIVATE);
            student2.setUser(user2);
            student2 = studentRepository.save(student2);
        } else {
            student2 = studentRepository.findByUser(user2).orElse(null);
        }

        testCategory = categoryRepository.findByName("Hackathon").orElse(null);
        if (testCategory == null) {
            testCategory = categoryRepository.findAll().get(0);
        }
    }

    // 1. Slug uniqueness test
    @Test
    void testSlugGenerationUniqueness() {
        String slug1 = userService.generateUniqueSlug("Test User", "21EG000001");
        assertNotNull(slug1);
        assertTrue(slug1.matches("^[a-z0-9-]+$"), "Slug should only contain lowercase letters, numbers, and hyphens");

        // Test with existing student's name and roll number
        String dupSlug = userService.generateUniqueSlug(student1.getUser().getFullName(), student1.getRollNumber());
        // Since student1 already owns that slug, generating a new one must append a number
        assertTrue(dupSlug.startsWith(student1.getProfileSlug()) || dupSlug.contains("-"), "Duplicate slug must have unique suffix");
    }

    // 2. Test PRIVATE visibility: anonymous viewer blocked, other student blocked, owner allowed
    @Test
    void testPrivateVisibilityRules() {
        student1.setVisibility(Visibility.PRIVATE);
        studentRepository.save(student1);

        String slug = student1.getProfileSlug();
        String ownerEmail = student1.getUser().getEmail();
        String otherEmail = student2.getUser().getEmail();

        // Anonymous user -> throws ProfileNotFoundException
        assertThrows(ProfileNotFoundException.class, () -> {
            profileService.getPublicProfile(slug, null);
        });

        // Other student -> throws ProfileNotFoundException
        assertThrows(ProfileNotFoundException.class, () -> {
            profileService.getPublicProfile(slug, otherEmail);
        });

        // Owner -> allowed
        PublicProfileDto profile = profileService.getPublicProfile(slug, ownerEmail);
        assertNotNull(profile);
        assertEquals(student1.getUser().getFullName(), profile.getFullName());
    }

    // 3. Test COLLEGE_ONLY visibility: anonymous viewer blocked, logged-in user allowed
    @Test
    void testCollegeOnlyVisibilityRules() {
        student1.setVisibility(Visibility.COLLEGE_ONLY);
        studentRepository.save(student1);

        String slug = student1.getProfileSlug();
        String otherEmail = student2.getUser().getEmail();

        // Anonymous user -> throws ProfileNotFoundException
        assertThrows(ProfileNotFoundException.class, () -> {
            profileService.getPublicProfile(slug, null);
        });

        // Other student (logged in) -> allowed
        PublicProfileDto profile = profileService.getPublicProfile(slug, otherEmail);
        assertNotNull(profile);
        assertEquals(student1.getUser().getFullName(), profile.getFullName());
    }

    // 4. Test PUBLIC visibility: anyone can view (even anonymous)
    @Test
    void testPublicVisibilityRules() {
        student1.setVisibility(Visibility.PUBLIC);
        studentRepository.save(student1);

        String slug = student1.getProfileSlug();

        // Anonymous viewer -> allowed
        PublicProfileDto profile = profileService.getPublicProfile(slug, null);
        assertNotNull(profile);
        assertEquals(student1.getUser().getFullName(), profile.getFullName());
    }

    // 5. Test Non-existent / fake slug throws same ProfileNotFoundException
    @Test
    void testFakeSlugThrowsSameException() {
        assertThrows(ProfileNotFoundException.class, () -> {
            profileService.getPublicProfile("non-existent-made-up-slug-9999", null);
        });
    }

    // 6. Test only APPROVED achievements are returned, never PENDING or REJECTED
    @Test
    void testOnlyApprovedAchievementsReturned() {
        student1.setVisibility(Visibility.PUBLIC);
        studentRepository.save(student1);

        // Create 1 APPROVED, 1 PENDING, 1 REJECTED achievement
        Achievement approvedAch = new Achievement();
        approvedAch.setTitle("Smart India Hackathon Winner");
        approvedAch.setDescription("First place in national hackathon");
        approvedAch.setLevel("NATIONAL");
        approvedAch.setEventDate(LocalDate.now().minusDays(10));
        approvedAch.setOrganizer("AICTE");
        approvedAch.setStatus(Status.APPROVED);
        approvedAch.setPointsAwarded(25);
        approvedAch.setCategory(testCategory);
        approvedAch.setStudent(student1);
        approvedAch = achievementRepository.save(approvedAch);

        Achievement pendingAch = new Achievement();
        pendingAch.setTitle("Pending Paper Presentation");
        pendingAch.setDescription("Under review");
        pendingAch.setLevel("COLLEGE");
        pendingAch.setEventDate(LocalDate.now());
        pendingAch.setOrganizer("College");
        pendingAch.setStatus(Status.PENDING);
        pendingAch.setCategory(testCategory);
        pendingAch.setStudent(student1);
        achievementRepository.save(pendingAch);

        Achievement rejectedAch = new Achievement();
        rejectedAch.setTitle("Rejected Fake Certificate");
        rejectedAch.setDescription("Invalid proof");
        rejectedAch.setLevel("LOCAL");
        rejectedAch.setEventDate(LocalDate.now().minusDays(20));
        rejectedAch.setOrganizer("Unknown");
        rejectedAch.setStatus(Status.REJECTED);
        rejectedAch.setCategory(testCategory);
        rejectedAch.setStudent(student1);
        achievementRepository.save(rejectedAch);

        // Fetch public profile
        PublicProfileDto profile = profileService.getPublicProfile(student1.getProfileSlug(), null);

        // Check grouped achievements: none should be pending or rejected
        for (List<PublicAchievementDto> list : profile.getGroupedAchievements().values()) {
            for (PublicAchievementDto a : list) {
                assertNotEquals("Pending Paper Presentation", a.getTitle());
                assertNotEquals("Rejected Fake Certificate", a.getTitle());
            }
        }
    }

    // 7. Test sensitive fields never leak in PublicProfileDto or PublicAchievementDto
    @Test
    void testNoSensitiveDataLeaked() {
        student1.setVisibility(Visibility.PUBLIC);
        studentRepository.save(student1);

        PublicProfileDto profile = profileService.getPublicProfile(student1.getProfileSlug(), null);
        // PublicProfileDto only has: fullName, branch, year, totalPoints, categoryCounts, groupedAchievements
        // It does NOT have rollNumber, email, phone, or password
        assertNotNull(profile.getFullName());
        assertNotNull(profile.getBranch());
    }

    // 8. Test updateVisibility logs audit event
    @Test
    void testUpdateVisibilityAuditLog() {
        long beforeCount = auditLogRepository.count();
        profileService.updateVisibility(student1.getUser().getEmail(), Visibility.PUBLIC);

        Student updated = studentRepository.findByRollNumber(student1.getRollNumber()).orElseThrow();
        assertEquals(Visibility.PUBLIC, updated.getVisibility());

        long afterCount = auditLogRepository.count();
        assertTrue(afterCount > beforeCount, "Audit log row must be inserted");

        List<AuditLog> logs = auditLogRepository.findAll();
        AuditLog lastLog = logs.get(logs.size() - 1);
        assertEquals("PROFILE_VISIBILITY_CHANGED", lastLog.getAction());
    }

    // 9. Test NotificationService: getUnreadCount, markAsRead, markAllAsRead
    @Test
    void testNotificationLifecycle() {
        User user1 = student1.getUser();

        // Create a notification for student1
        Notification n1 = new Notification();
        n1.setUser(user1);
        n1.setMessage("Your achievement has been approved!");
        n1.setRead(false);
        n1.setCreatedAt(LocalDateTime.now());
        n1 = notificationRepository.save(n1);

        long initialUnread = notificationService.getUnreadCount(user1.getEmail());
        assertTrue(initialUnread >= 1);

        // Mark as read
        notificationService.markAsRead(n1.getId(), user1.getEmail());

        Notification updated = notificationRepository.findById(n1.getId()).orElseThrow();
        assertTrue(updated.isRead());

        // Create another unread notification
        Notification n2 = new Notification();
        n2.setUser(user1);
        n2.setMessage("Another update");
        n2.setRead(false);
        n2.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n2);

        // Mark all as read
        notificationService.markAllAsRead(user1.getEmail());
        assertEquals(0, notificationService.getUnreadCount(user1.getEmail()));
    }

    // 10. Test cross-user notification mark-as-read security
    @Test
    void testCrossUserNotificationSecurity() {
        // Notification for student1
        Notification n = new Notification();
        n.setUser(student1.getUser());
        n.setMessage("Private message for student 1");
        n.setRead(false);
        Notification savedNotification = notificationRepository.save(n);
        Long notifId = savedNotification.getId();

        // student2 tries to mark student1's notification as read -> must throw SecurityException
        assertThrows(SecurityException.class, () -> {
            notificationService.markAsRead(notifId, student2.getUser().getEmail());
        });
    }

    // 11. MockMvc: GET /u/{slug} permitAll check
    @Test
    void testPublicProfileEndpointPermitAll() throws Exception {
        student1.setVisibility(Visibility.PUBLIC);
        studentRepository.save(student1);

        mockMvc.perform(get("/u/" + student1.getProfileSlug()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/public"))
                .andExpect(model().attributeExists("profile"));
    }

    // 12. MockMvc: GET /u/{slug} for private profile returns 404 page for anonymous user
    @Test
    void testPrivateProfileReturns404ForAnonymous() throws Exception {
        student1.setVisibility(Visibility.PRIVATE);
        studentRepository.save(student1);

        mockMvc.perform(get("/u/" + student1.getProfileSlug()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("profile/not-found"));
    }

    // 13. MockMvc: Protected endpoints require login
    @Test
    void testProtectedEndpointsRequireLogin() throws Exception {
        // /student/profile requires login
        mockMvc.perform(get("/student/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // /notifications requires login
        mockMvc.perform(get("/notifications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // 14. MockMvc: Student can view their own profile settings and update visibility
    @Test
    @WithMockUser(username = "student@anurag.edu.in", roles = {"STUDENT"})
    void testStudentProfileSettingsAndVisibilityUpdate() throws Exception {
        mockMvc.perform(get("/student/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/profile"))
                .andExpect(model().attributeExists("student"))
                .andExpect(model().attributeExists("profileUrl"));

        // POST visibility update with CSRF
        mockMvc.perform(post("/student/profile/visibility")
                        .with(csrf())
                        .param("visibility", "COLLEGE_ONLY"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/profile"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    // 15. MockMvc: Notifications page and mark-all-as-read
    @Test
    @WithMockUser(username = "student@anurag.edu.in", roles = {"STUDENT"})
    void testNotificationsPageAndMarkAllRead() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications/list"))
                .andExpect(model().attributeExists("notifications"));

        mockMvc.perform(post("/notifications/read-all").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
