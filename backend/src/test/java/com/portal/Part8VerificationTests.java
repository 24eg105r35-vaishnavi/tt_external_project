package com.portal;

import com.portal.dto.AnalyticsSummaryDto;
import com.portal.dto.ChartItemDto;
import com.portal.dto.LeaderboardRowDto;
import com.portal.entity.*;
import com.portal.repository.*;
import com.portal.security.LoginLockoutListener;
import com.portal.service.AnalyticsService;
import com.portal.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Part8VerificationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private LoginLockoutListener loginLockoutListener;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Student testStudent;
    private Student privateStudent;
    private Category testCategory;

    @BeforeEach
    public void setup() {
        Role studentRole = roleRepository.findByName("STUDENT").orElseGet(() -> roleRepository.save(new Role("STUDENT")));

        // create/find public test student
        User u1 = userRepository.findByEmail("part8_public@anurag.edu.in").orElseGet(() -> {
            User user = new User();
            user.setEmail("part8_public@anurag.edu.in");
            user.setPassword(passwordEncoder.encode("pass123"));
            user.setFullName("Part8 Public Student");
            user.setRole(studentRole);
            user.setEnabled(true);
            return userRepository.save(user);
        });

        testStudent = studentRepository.findByRollNumber("21EG105P81").orElseGet(() -> {
            Student s = new Student();
            s.setUser(u1);
            s.setRollNumber("21EG105P81");
            s.setBranch("CSE");
            s.setYear(3);
            s.setProfileSlug("part8-public-student");
            s.setVisibility(Visibility.PUBLIC);
            return studentRepository.save(s);
        });
        testStudent.setVisibility(Visibility.PUBLIC);
        studentRepository.save(testStudent);

        // create/find private test student
        User u2 = userRepository.findByEmail("part8_private@anurag.edu.in").orElseGet(() -> {
            User user = new User();
            user.setEmail("part8_private@anurag.edu.in");
            user.setPassword(passwordEncoder.encode("pass123"));
            user.setFullName("Part8 Private Student");
            user.setRole(studentRole);
            user.setEnabled(true);
            return userRepository.save(user);
        });

        privateStudent = studentRepository.findByRollNumber("21EG105P82").orElseGet(() -> {
            Student s = new Student();
            s.setUser(u2);
            s.setRollNumber("21EG105P82");
            s.setBranch("ECE");
            s.setYear(2);
            s.setProfileSlug("part8-private-student");
            s.setVisibility(Visibility.PRIVATE);
            return studentRepository.save(s);
        });

        testCategory = categoryRepository.findByName("Hackathon").orElseGet(() -> {
            Category c = new Category();
            c.setName("Hackathon");
            c.setDescription("Coding Hackathon");
            c.setPoints(12);
            c.setVerifierDepartment("ALL");
            return categoryRepository.save(c);
        });

        // Ensure at least one approved achievement for public student
        List<Achievement> achList = achievementRepository.findByStudentAndStatus(testStudent, Status.APPROVED);
        if (achList.isEmpty()) {
            Achievement ach = new Achievement();
            ach.setTitle("Part 8 National Hackathon");
            ach.setDescription("Won first prize");
            ach.setLevel("NATIONAL");
            ach.setEventDate(LocalDate.now().minusMonths(1));
            ach.setOrganizer("AICTE");
            ach.setStatus(Status.APPROVED);
            ach.setPointsAwarded(12);
            ach.setStudent(testStudent);
            ach.setCategory(testCategory);
            achievementRepository.save(ach);
        }
    }

    // 1. Analytics Service Tests
    @Test
    public void testAnalyticsAggregationsAndSummary() {
        AnalyticsSummaryDto summary = analyticsService.getSummary(null, null);
        assertNotNull(summary);
        assertTrue(summary.getTotalApproved() >= 1);
        assertTrue(summary.getTotalStudents() >= 1);
        assertTrue(summary.getTotalPoints() >= 12);

        List<ChartItemDto> byCat = analyticsService.getByCategory(null, null);
        assertNotNull(byCat);
        assertFalse(byCat.isEmpty());
        assertTrue(byCat.get(0).getPercentage() <= 100);

        List<ChartItemDto> byBranch = analyticsService.getByBranch(null, null);
        assertNotNull(byBranch);
        assertFalse(byBranch.isEmpty());

        List<ChartItemDto> byLevel = analyticsService.getByLevel(null, null);
        assertNotNull(byLevel);
        assertFalse(byLevel.isEmpty());

        List<ChartItemDto> byYear = analyticsService.getByYear(null, null);
        assertNotNull(byYear);
        assertFalse(byYear.isEmpty());
    }

    // 2. Analytics Route Authorization Test
    @Test
    public void testAnalyticsRouteRequiresFacultyOrAdmin() throws Exception {
        // Unauthenticated access redirects to login
        mockMvc.perform(get("/analytics"))
                .andExpect(status().is3xxRedirection());

        // Student access should be forbidden (403)
        mockMvc.perform(get("/analytics").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student@anurag.edu.in").roles("STUDENT")))
                .andExpect(status().isForbidden());

        // Faculty access allowed
        mockMvc.perform(get("/analytics").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("faculty@anurag.edu.in").roles("FACULTY")))
                .andExpect(status().isOk())
                .andExpect(view().name("analytics/dashboard"))
                .andExpect(model().attributeExists("summary", "byCategory", "byBranch", "byLevel", "byYear"));

        // Admin access allowed
        mockMvc.perform(get("/analytics").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@anurag.edu.in").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    // 3. Leaderboard Query & Route Tests
    @Test
    public void testLeaderboardSkipsPrivateProfiles() {
        List<Object[]> topStudents = achievementRepository.findTopStudentsLeaderboard(null, null, PageRequest.of(0, 10));
        assertNotNull(topStudents);

        for (Object[] row : topStudents) {
            String name = (String) row[0];
            assertNotEquals("Part8 Private Student", name, "Private students must never appear in leaderboard!");
        }
    }

    @Test
    @WithMockUser(username = "student@anurag.edu.in", roles = {"STUDENT"})
    public void testLeaderboardEndpointAccess() throws Exception {
        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("leaderboard/list"))
                .andExpect(model().attributeExists("leaderboard", "branches", "years"));
    }

    // 4. SEO & Robots & Sitemap Tests
    @Test
    public void testRobotsTxtEndpoint() throws Exception {
        mockMvc.perform(get("/robots.txt"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Allow: /u/")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Disallow: /admin/")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sitemap:")));
    }

    @Test
    public void testSitemapXmlEndpoint() throws Exception {
        mockMvc.perform(get("/sitemap.xml"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/xml"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<urlset")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/u/" + testStudent.getProfileSlug())))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("/u/" + privateStudent.getProfileSlug()))));
    }

    @Test
    public void testPublicProfileSeoTagsAndJsonLd() throws Exception {
        mockMvc.perform(get("/u/" + testStudent.getProfileSlug()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("pageTitle", "metaDescription", "ogTitle", "canonicalUrl", "jsonLd"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Anurag University | Verified Achievements")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("https://schema.org")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Part8 Public Student")));
    }

    // 5. Security Polish: 5 Wrong Logins Lockout & AuditLog
    @Test
    public void testLoginLockoutAfterFiveFailedAttempts() {
        User user = userRepository.findByEmail("part8_public@anurag.edu.in").orElseThrow();
        user.setFailedAttempts(0);
        user.setLockUntil(null);
        userRepository.save(user);

        Authentication auth = new UsernamePasswordAuthenticationToken("part8_public@anurag.edu.in", "wrongpass");
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        // trigger 4 failures
        for (int i = 0; i < 4; i++) {
            loginLockoutListener.onAuthenticationFailure(new AuthenticationFailureBadCredentialsEvent(auth, ex));
        }
        User after4 = userRepository.findByEmail("part8_public@anurag.edu.in").orElseThrow();
        assertEquals(4, after4.getFailedAttempts());
        assertNull(after4.getLockUntil());

        // 5th failure triggers 15 minute lock
        loginLockoutListener.onAuthenticationFailure(new AuthenticationFailureBadCredentialsEvent(auth, ex));
        User after5 = userRepository.findByEmail("part8_public@anurag.edu.in").orElseThrow();
        assertEquals(5, after5.getFailedAttempts());
        assertNotNull(after5.getLockUntil());
        assertTrue(after5.getLockUntil().isAfter(LocalDateTime.now()));

        // check AuditLog contains ACCOUNT_LOCKED
        List<AuditLog> logs = auditLogRepository.findAll();
        boolean foundLockedAudit = logs.stream().anyMatch(l -> "ACCOUNT_LOCKED".equals(l.getAction()) && "part8_public@anurag.edu.in".equals(l.getPerformedBy()));
        assertTrue(foundLockedAudit, "AuditLog must contain ACCOUNT_LOCKED entry");

        // success event resets lockout
        Authentication successAuth = new UsernamePasswordAuthenticationToken("part8_public@anurag.edu.in", "pass123");
        loginLockoutListener.onAuthenticationSuccess(new AuthenticationSuccessEvent(successAuth));

        User afterSuccess = userRepository.findByEmail("part8_public@anurag.edu.in").orElseThrow();
        assertEquals(0, afterSuccess.getFailedAttempts());
        assertNull(afterSuccess.getLockUntil());
    }

    // 6. Security Headers Test
    @Test
    public void testSecurityHeadersPresent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("Referrer-Policy"));
    }

    // 7. Magic Byte File Validation Test
    @Test
    public void testMagicByteValidationRejectsSpoofedExe() {
        // exe content renamed to .pdf
        byte[] fakePdfBytes = new byte[]{'M', 'Z', 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile spoofedFile = new MockMultipartFile("file", "virus.pdf", "application/pdf", fakePdfBytes);

        assertThrows(Exception.class, () -> {
            fileStorageService.storeFile(spoofedFile, testStudent);
        });
    }

    // 8. Custom Error Pages (404, 403, 500)
    @Test
    public void testErrorPagesRender() throws Exception {
        mockMvc.perform(get("/error"))
                .andExpect(status().is5xxServerError());

        com.portal.controller.GlobalExceptionHandler handler = new com.portal.controller.GlobalExceptionHandler();
        org.springframework.http.ResponseEntity<java.util.Map<String, Object>> response = handler.handleProfileNotFound(new com.portal.util.ProfileNotFoundException("Not found"));
        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not found", response.getBody().get("message"));
    }
}
