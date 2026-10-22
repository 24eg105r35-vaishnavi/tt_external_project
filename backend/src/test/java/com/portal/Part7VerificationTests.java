package com.portal;

import com.portal.dto.FacultyCreateDto;
import com.portal.dto.SearchDto;
import com.portal.entity.*;
import com.portal.repository.*;
import com.portal.security.CustomUserDetailsService;
import com.portal.service.AdminUserService;
import com.portal.service.CategoryService;
import com.portal.service.ExportService;
import com.portal.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Part7VerificationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

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
    private AuditLogRepository auditLogRepository;

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Student student1;
    private Student student2;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        student1 = studentRepository.findByRollNumber("21EG105A01").orElse(null);
        assertNotNull(student1, "Seeded student1 must exist");

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();

        // Create student 2 in ECE if not existing
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
            student2.setYear(2);
            student2.setPhone("9876543299");
            student2.setProfileSlug("sneha-reddy-21eg105a99");
            student2.setVisibility(Visibility.PUBLIC);
            student2.setUser(user2);
            student2 = studentRepository.save(student2);
        } else {
            student2 = studentRepository.findByUser(user2).orElse(null);
        }

        testCategory = categoryRepository.findByName("Hackathon").orElse(null);
        if (testCategory == null) {
            testCategory = categoryRepository.findAll().get(0);
        }

        // Clean prior test achievements (delete child records first to respect foreign keys)
        verificationRepository.deleteAll();
        documentRepository.deleteAll();
        achievementRepository.deleteAll();

        // Seed 3 approved achievements with different branches, levels, and dates
        Achievement a1 = new Achievement();
        a1.setTitle("National AI Hackathon Winner");
        a1.setDescription("First prize");
        a1.setLevel("NATIONAL");
        a1.setEventDate(LocalDate.of(2026, 1, 15));
        a1.setOrganizer("AICTE");
        a1.setStatus(Status.APPROVED);
        a1.setPointsAwarded(25);
        a1.setCategory(testCategory);
        a1.setStudent(student1);
        achievementRepository.save(a1);

        Achievement a2 = new Achievement();
        a2.setTitle("State Level Robotics Expo");
        a2.setDescription("Robotics champion");
        a2.setLevel("STATE");
        a2.setEventDate(LocalDate.of(2026, 2, 20));
        a2.setOrganizer("State Tech Council");
        a2.setStatus(Status.APPROVED);
        a2.setPointsAwarded(15);
        a2.setCategory(testCategory);
        a2.setStudent(student2);
        achievementRepository.save(a2);

        // Also add 1 PENDING and 1 REJECTED achievement
        Achievement aPending = new Achievement();
        aPending.setTitle("Pending Web Dev Contest");
        aPending.setLevel("COLLEGE");
        aPending.setEventDate(LocalDate.of(2026, 3, 10));
        aPending.setStatus(Status.PENDING);
        aPending.setCategory(testCategory);
        aPending.setStudent(student1);
        achievementRepository.save(aPending);

        Achievement aRejected = new Achievement();
        aRejected.setTitle("Rejected Fake Certification");
        aRejected.setLevel("COLLEGE");
        aRejected.setEventDate(LocalDate.of(2026, 3, 5));
        aRejected.setStatus(Status.REJECTED);
        aRejected.setCategory(testCategory);
        aRejected.setStudent(student2);
        achievementRepository.save(aRejected);
    }

    // 1. Search by student name
    @Test
    void testSearchByStudentName() {
        SearchDto dto = new SearchDto();
        dto.setKeyword("Rahul");

        Page<Achievement> results = searchService.search(dto, 0);
        assertEquals(1, results.getTotalElements());
        assertEquals("National AI Hackathon Winner", results.getContent().get(0).getTitle());
    }

    // 2. Search by roll number
    @Test
    void testSearchByRollNumber() {
        SearchDto dto = new SearchDto();
        dto.setKeyword("21EG105A99");

        Page<Achievement> results = searchService.search(dto, 0);
        assertEquals(1, results.getTotalElements());
        assertEquals("State Level Robotics Expo", results.getContent().get(0).getTitle());
    }

    // 3. Search by branch and year
    @Test
    void testSearchByBranchAndYear() {
        SearchDto dto = new SearchDto();
        dto.setBranch("CSE");
        dto.setYear(3);

        Page<Achievement> results = searchService.search(dto, 0);
        assertEquals(1, results.getTotalElements());
        assertEquals("CSE", results.getContent().get(0).getStudent().getBranch());
    }

    // 4. Search by level and category
    @Test
    void testSearchByLevelAndCategory() {
        SearchDto dto = new SearchDto();
        dto.setLevel("NATIONAL");
        dto.setCategoryId(testCategory.getId());

        Page<Achievement> results = searchService.search(dto, 0);
        assertEquals(1, results.getTotalElements());
        assertEquals("NATIONAL", results.getContent().get(0).getLevel());
    }

    // 5. Search by date range
    @Test
    void testSearchByDateRange() {
        SearchDto dto = new SearchDto();
        dto.setFromDate(LocalDate.of(2026, 2, 1));
        dto.setToDate(LocalDate.of(2026, 2, 28));

        Page<Achievement> results = searchService.search(dto, 0);
        assertEquals(1, results.getTotalElements());
        assertEquals("State Level Robotics Expo", results.getContent().get(0).getTitle());
    }

    // 6. Confirm PENDING and REJECTED items never appear in search results
    @Test
    void testPendingAndRejectedNeverAppearInSearch() {
        SearchDto dto = new SearchDto(); // no filters -> returns all approved items

        Page<Achievement> results = searchService.search(dto, 0);
        for (Achievement a : results.getContent()) {
            assertEquals(Status.APPROVED, a.getStatus(), "Only APPROVED achievements should appear");
            assertNotEquals("Pending Web Dev Contest", a.getTitle());
            assertNotEquals("Rejected Fake Certification", a.getTitle());
        }
    }

    // 7. CSV Export: UTF-8 BOM, formula injection defense, and comma escaping
    @Test
    void testCsvExportSecurityAndFormatting() {
        // Create an achievement with formula injection attempt and commas
        Achievement dangerousAch = new Achievement();
        dangerousAch.setTitle("=1+1, Formula Test");
        dangerousAch.setDescription("Formula test");
        dangerousAch.setLevel("COLLEGE");
        dangerousAch.setEventDate(LocalDate.now());
        dangerousAch.setPointsAwarded(5);
        dangerousAch.setStatus(Status.APPROVED);
        dangerousAch.setCategory(testCategory);
        dangerousAch.setStudent(student1);

        byte[] csvBytes = exportService.generateAchievementsCsv(List.of(dangerousAch));
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        // Check UTF-8 BOM is present at the beginning
        assertTrue(csvContent.startsWith("\uFEFF"), "CSV must start with UTF-8 BOM for Microsoft Excel");

        // Check formula '=1+1' was prefixed with single quote and escaped for commas
        assertTrue(csvContent.contains("\"'=1+1, Formula Test\""), "Formula injection must be neutralized with a single quote and wrapped in double quotes");
    }

    // 8. Security: Students cannot access /search or /export (403 Forbidden)
    @Test
    @WithMockUser(username = "student@anurag.edu.in", roles = {"STUDENT"})
    void testStudentAccessDeniedToSearchAndExport() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/export/csv"))
                .andExpect(status().isForbidden());
    }

    // 9. Security: Faculty and Admin can access /search and /export/csv
    @Test
    @WithMockUser(username = "faculty@anurag.edu.in", roles = {"FACULTY"})
    void testFacultyCanAccessSearchAndExport() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("search/search"))
                .andExpect(model().attributeExists("results"));

        mockMvc.perform(get("/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(header().exists("Content-Disposition"));
    }

    // 10. Admin Categories: Validate JSON schema before saving
    @Test
    void testCategoryJsonSchemaValidation() {
        Category invalidCat = new Category();
        String catName = "Test Category " + System.currentTimeMillis();
        invalidCat.setName(catName);
        invalidCat.setPoints(10);
        invalidCat.setVerifierDepartment("ALL");
        invalidCat.setFieldSchema("{ this is not valid json }");

        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.saveCategory(invalidCat, "admin@anurag.edu.in");
        });

        // Valid JSON works
        invalidCat.setFieldSchema("[{\"name\":\"rank\",\"type\":\"text\",\"label\":\"Rank\"}]");
        Category saved = categoryService.saveCategory(invalidCat, "admin@anurag.edu.in");
        assertNotNull(saved.getId());

        // Deactivate category
        categoryService.toggleActive(saved.getId(), "admin@anurag.edu.in");
        Category toggled = categoryService.getCategoryById(saved.getId());
        assertFalse(toggled.isActive());

        // Inactive category does not appear in findByActiveTrue()
        List<Category> activeCategories = categoryRepository.findByActiveTrue();
        assertFalse(activeCategories.stream().anyMatch(c -> c.getId().equals(saved.getId())));
    }

    // 11. Admin Users: Create faculty user
    @Test
    void testCreateFacultyUser() {
        FacultyCreateDto dto = new FacultyCreateDto();
        dto.setFullName("Dr. Rajesh Sharma");
        dto.setEmail("faculty_" + System.currentTimeMillis() + "@anurag.edu.in");
        dto.setDepartment("CSE");
        dto.setDesignation("Associate Professor");
        dto.setPassword("faculty123");

        User created = adminUserService.createFacultyUser(dto, "admin@anurag.edu.in");
        assertNotNull(created.getId());
        assertEquals("FACULTY", created.getRole().getName());
        assertTrue(passwordEncoder.matches("faculty123", created.getPassword()));
    }

    // 12. Admin Users: Admin cannot disable their own account
    @Test
    void testAdminCannotDisableSelf() {
        User adminUser = userRepository.findByEmail("admin@anurag.edu.in").orElseThrow();

        assertThrows(IllegalArgumentException.class, () -> {
            adminUserService.toggleUserStatus(adminUser.getId(), "admin@anurag.edu.in");
        });
    }

    // 13. Admin Users: Disabled user cannot log in
    @Test
    void testDisabledUserCannotLogin() {
        // Disable student2
        adminUserService.toggleUserStatus(student2.getUser().getId(), "admin@anurag.edu.in");

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("student2@anurag.edu.in");
        });

        // Re-enable
        adminUserService.toggleUserStatus(student2.getUser().getId(), "admin@anurag.edu.in");
        assertNotNull(customUserDetailsService.loadUserByUsername("student2@anurag.edu.in"));
    }

    // 14. Admin Audit Logs: View audit logs and search by action
    @Test
    @WithMockUser(username = "admin@anurag.edu.in", roles = {"ADMIN"})
    void testAdminAuditLogViewer() throws Exception {
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/audit"))
                .andExpect(model().attributeExists("auditPage"));
    }
}
