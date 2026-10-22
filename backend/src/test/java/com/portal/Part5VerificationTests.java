package com.portal;

import com.portal.dto.AchievementDto;
import com.portal.dto.DecisionDto;
import com.portal.entity.Achievement;
import com.portal.entity.Category;
import com.portal.entity.Document;
import com.portal.entity.Role;
import com.portal.entity.Status;
import com.portal.entity.Student;
import com.portal.entity.User;
import com.portal.entity.Verification;
import com.portal.entity.Visibility;
import com.portal.repository.AchievementRepository;
import com.portal.repository.CategoryRepository;
import com.portal.repository.DocumentRepository;
import com.portal.repository.RoleRepository;
import com.portal.repository.StudentRepository;
import com.portal.repository.UserRepository;
import com.portal.repository.VerificationRepository;
import com.portal.service.AchievementService;
import com.portal.service.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class Part5VerificationTests {

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Student testStudent;
    private Student otherStudent;
    private Category hackathonCategory;
    private Category sportsCategory;

    @BeforeEach
    void setUp() {
        // Ensure student 1 exists
        testStudent = studentRepository.findByRollNumber("21EG105A01").orElse(null);
        assertNotNull(testStudent, "Test student 1 should be seeded");

        // Ensure student 2 exists for cross-student security testing
        Role studentRole = roleRepository.findByName("STUDENT").orElse(null);
        assertNotNull(studentRole, "STUDENT role must exist");

        User student2User = userRepository.findByEmail("student2@anurag.edu.in").orElse(null);
        if (student2User == null) {
            student2User = new User();
            student2User.setEmail("student2@anurag.edu.in");
            student2User.setPassword(passwordEncoder.encode("student123"));
            student2User.setFullName("Sneha Reddy");
            student2User.setEnabled(true);
            student2User.setRole(studentRole);
            student2User = userRepository.save(student2User);

            otherStudent = new Student();
            otherStudent.setRollNumber("21EG105A99");
            otherStudent.setBranch("ECE");
            otherStudent.setYear(3);
            otherStudent.setPhone("9876543299");
            otherStudent.setProfileSlug("21eg105a99-sneha-reddy");
            otherStudent.setVisibility(Visibility.PRIVATE);
            otherStudent.setUser(student2User);
            otherStudent = studentRepository.save(otherStudent);
        } else {
            otherStudent = studentRepository.findByUser(student2User).orElse(null);
        }

        hackathonCategory = categoryRepository.findByName("Hackathon").orElseThrow();
        sportsCategory = categoryRepository.findByName("Sports").orElseThrow();
    }

    private Achievement createTestAchievement(Category category, String title, Student student) {
        Achievement a = new Achievement();
        a.setTitle(title);
        a.setDescription("Test submission for " + title);
        a.setLevel("NATIONAL");
        a.setEventDate(LocalDate.now());
        a.setOrganizer("AICTE");
        a.setStatus(Status.PENDING);
        a.setPointsAwarded(0);
        a.setCategory(category);
        a.setStudent(student);
        a.setCreatedAt(LocalDateTime.now());
        Achievement saved = achievementRepository.save(a);

        // attach a mock document
        Document doc = new Document();
        doc.setOriginalName("certificate.pdf");
        doc.setStoredName("test-" + saved.getId() + ".pdf");
        doc.setFilePath("uploads/test-" + saved.getId() + ".pdf");
        doc.setFileType("application/pdf");
        doc.setFileSize(2048);
        doc.setChecksum("d41d8cd98f00b204e9800998ecf8427e");
        doc.setUploadedAt(LocalDateTime.now());
        doc.setAchievement(saved);
        documentRepository.save(doc);

        return saved;
    }

    @Test
    void testFacultyQueueAndDepartmentRouting() {
        // Create Hackathon (verifierDepartment = ALL) and Sports (verifierDepartment = Physical Education)
        Achievement hackathon = createTestAchievement(hackathonCategory, "SIH 2026 Test", testStudent);
        Achievement sports = createTestAchievement(sportsCategory, "Inter-College Cricket 2026", testStudent);

        // 1. CSE Faculty: should see Hackathon (ALL), but NOT Sports (Physical Education)
        List<Achievement> cseQueue = verificationService.getQueueForFaculty("faculty@anurag.edu.in", null, null, null);
        boolean cseHasHackathon = false;
        boolean cseHasSports = false;
        for (Achievement a : cseQueue) {
            if (a.getId().equals(hackathon.getId())) cseHasHackathon = true;
            if (a.getId().equals(sports.getId())) cseHasSports = true;
        }
        assertTrue(cseHasHackathon, "CSE Faculty should see Hackathon claim because verifierDepartment is ALL");
        assertFalse(cseHasSports, "CSE Faculty must NOT see Sports claim because verifierDepartment is Physical Education");

        // 2. Physical Education Faculty: should see Sports claim
        List<Achievement> sportsQueue = verificationService.getQueueForFaculty("sports_faculty@anurag.edu.in", null, null, null);
        boolean sportsFacultyHasSports = false;
        for (Achievement a : sportsQueue) {
            if (a.getId().equals(sports.getId())) sportsFacultyHasSports = true;
        }
        assertTrue(sportsFacultyHasSports, "Physical Education Faculty must see Sports claim");
    }

    @Test
    void testRejectWithoutReasonMustBeRefused() {
        Achievement achievement = createTestAchievement(hackathonCategory, "Reject Without Reason Test", testStudent);

        // Reject with empty reason -> must throw IllegalArgumentException
        DecisionDto emptyReasonDto = new DecisionDto("REJECT", "   ");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            verificationService.decide(achievement.getId(), emptyReasonDto, "faculty@anurag.edu.in");
        });
        assertTrue(ex.getMessage().contains("Reason is required"));

        // Send back with null reason -> must throw IllegalArgumentException
        DecisionDto nullReasonDto = new DecisionDto("SEND_BACK", null);
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
            verificationService.decide(achievement.getId(), nullReasonDto, "faculty@anurag.edu.in");
        });
        assertTrue(ex2.getMessage().contains("Reason is required"));
    }

    @Test
    void testApproveAwardsPointsAndUpdatesStats() {
        Achievement achievement = createTestAchievement(hackathonCategory, "Approve Points Test", testStudent);

        DecisionDto approveDto = new DecisionDto("APPROVE", "Great accomplishment!");
        verificationService.decide(achievement.getId(), approveDto, "faculty@anurag.edu.in");

        // verify achievement updated
        Achievement updated = achievementRepository.findById(achievement.getId()).orElseThrow();
        assertEquals(Status.APPROVED, updated.getStatus());
        assertEquals(12, updated.getPointsAwarded(), "Approved hackathon must award 12 points");

        // verify new Verification row
        List<Verification> history = verificationRepository.findByAchievementOrderByDecidedAtDesc(updated);
        assertEquals(1, history.size());
        assertEquals("Great accomplishment!", history.get(0).getReason());

        // verify student dashboard stats sum totalPoints
        Map<String, Object> stats = achievementService.getStudentDashboardStats(testStudent.getUser().getEmail());
        assertTrue((int) stats.get("totalPoints") >= 12, "Dashboard total points must include 12 approved points");
        assertTrue((int) stats.get("approvedCount") >= 1);
    }

    @Test
    void testDoubleApprovePrevention() {
        Achievement achievement = createTestAchievement(hackathonCategory, "Double Click Test", testStudent);

        DecisionDto approveDto = new DecisionDto("APPROVE", "First approval");
        verificationService.decide(achievement.getId(), approveDto, "faculty@anurag.edu.in");

        // second attempt must be refused with "Already reviewed"
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            verificationService.decide(achievement.getId(), approveDto, "faculty@anurag.edu.in");
        });
        assertEquals("Already reviewed", ex.getMessage());
    }

    @Test
    void testSendBackStudentEditAndResubmit() {
        Achievement achievement = createTestAchievement(sportsCategory, "Cricket Tourney Send Back", testStudent);

        // 1. Faculty sends back
        DecisionDto sendBackDto = new DecisionDto("SEND_BACK", "Please upload a clearer certificate image.");
        verificationService.decide(achievement.getId(), sendBackDto, "sports_faculty@anurag.edu.in");

        Achievement sentBack = achievementRepository.findById(achievement.getId()).orElseThrow();
        assertEquals(Status.NEEDS_CORRECTION, sentBack.getStatus());
        assertEquals(0, sentBack.getPointsAwarded());

        // 2. Student edits and resubmits
        AchievementDto editDto = new AchievementDto();
        editDto.setCategoryId(sportsCategory.getId());
        editDto.setTitle("Cricket Tourney Resubmitted");
        editDto.setDescription("Updated description with match scores");
        editDto.setLevel("STATE");
        editDto.setEventDate(LocalDate.now());
        editDto.setOrganizer("Sports Authority of Telangana");

        Map<String, String> sportsExtraFields = new HashMap<>();
        sportsExtraFields.put("sport", "Cricket");
        sportsExtraFields.put("position", "1st Place");
        sportsExtraFields.put("individualOrTeam", "Team");

        Achievement resubmitted = achievementService.resubmitAchievement(
                sentBack.getId(), editDto, null, sportsExtraFields, testStudent.getUser().getEmail()
        );

        // 3. Status must be PENDING again
        assertEquals(Status.PENDING, resubmitted.getStatus());
        assertEquals("Cricket Tourney Resubmitted", resubmitted.getTitle());

        // 4. Old verification rows must stay intact
        List<Verification> history = verificationRepository.findByAchievementOrderByDecidedAtDesc(resubmitted);
        assertEquals(1, history.size(), "Old verification row must stay untouched");
        assertEquals("Please upload a clearer certificate image.", history.get(0).getReason());
    }

    @Test
    void testStudentCannotEditApprovedItem() {
        Achievement achievement = createTestAchievement(hackathonCategory, "Already Approved Claim", testStudent);
        verificationService.decide(achievement.getId(), new DecisionDto("APPROVE", "Approved"), "faculty@anurag.edu.in");

        AchievementDto editDto = new AchievementDto();
        editDto.setCategoryId(hackathonCategory.getId());
        editDto.setTitle("Trying to edit approved claim");
        editDto.setLevel("NATIONAL");
        editDto.setEventDate(LocalDate.now());
        editDto.setOrganizer("AICTE");

        // Must throw IllegalStateException
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            achievementService.resubmitAchievement(
                    achievement.getId(), editDto, null, new HashMap<>(), testStudent.getUser().getEmail()
            );
        });
        assertTrue(ex.getMessage().contains("Cannot edit achievement"));
    }

    @Test
    void testCrossStudentDetailAccessRefused() {
        Achievement achievement = createTestAchievement(hackathonCategory, "Student A Secret Claim", testStudent);

        // Student B tries to access Student A's achievement
        SecurityException ex = assertThrows(SecurityException.class, () -> {
            achievementService.getAchievementById(achievement.getId(), otherStudent.getUser().getEmail());
        });
        assertTrue(ex.getMessage().contains("permission"));
    }

    @Test
    void testFacultyAccessToUnmappedCategoryDocumentRefused() {
        // Sports belongs to "Physical Education"
        Achievement sportsAchievement = createTestAchievement(sportsCategory, "Athletics Meet", testStudent);
        List<Document> docs = documentRepository.findByAchievement(sportsAchievement);
        assertFalse(docs.isEmpty());
        Long docId = docs.get(0).getId();

        // CSE faculty (Computer Science and Engineering) tries to access Sports document -> Must be refused!
        SecurityException ex = assertThrows(SecurityException.class, () -> {
            verificationService.getDocumentForFaculty(docId, "faculty@anurag.edu.in");
        });
        assertTrue(ex.getMessage().contains("permission"));
    }
}
