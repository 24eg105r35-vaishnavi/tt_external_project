package com.portal.config;

import com.portal.entity.Category;
import com.portal.entity.Faculty;
import com.portal.entity.Role;
import com.portal.entity.Student;
import com.portal.entity.User;
import com.portal.entity.Visibility;
import com.portal.repository.CategoryRepository;
import com.portal.repository.FacultyRepository;
import com.portal.repository.RoleRepository;
import com.portal.repository.StudentRepository;
import com.portal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      CategoryRepository categoryRepository,
                      UserRepository userRepository,
                      FacultyRepository facultyRepository,
                      StudentRepository studentRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Roles
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role("STUDENT"));
            roleRepository.save(new Role("FACULTY"));
            roleRepository.save(new Role("ADMIN"));
            System.out.println("Seeded 3 roles: STUDENT, FACULTY, ADMIN");
        }

        // 2. Seed / Update Categories with proper verifierDepartment and JSON fieldSchema
        seedOrUpdateCategory("Certificate", "Course or skill certificate", 3, "ALL",
                "[{\"name\":\"issuer\",\"label\":\"Issuing Organization\",\"type\":\"text\",\"required\":true}]");

        seedOrUpdateCategory("Hackathon", "Coding hackathon", 12, "ALL",
                "[{\"name\":\"teamSize\",\"label\":\"Team Size\",\"type\":\"number\",\"required\":true}," +
                "{\"name\":\"role\",\"label\":\"Your Role\",\"type\":\"text\",\"required\":true}," +
                "{\"name\":\"outcome\",\"label\":\"Outcome\",\"type\":\"select\",\"options\":[\"Winner\",\"Runner-up\",\"Finalist\",\"Participant\"],\"required\":true}]");

        seedOrUpdateCategory("Sports", "Sports meet or tournament", 12, "Physical Education",
                "[{\"name\":\"sport\",\"label\":\"Sport Name\",\"type\":\"text\",\"required\":true}," +
                "{\"name\":\"position\",\"label\":\"Position / Standing\",\"type\":\"select\",\"options\":[\"1st Place\",\"2nd Place\",\"3rd Place\",\"Participant\"],\"required\":true}," +
                "{\"name\":\"individualOrTeam\",\"label\":\"Individual or Team\",\"type\":\"select\",\"options\":[\"Individual\",\"Team\"],\"required\":true}]");

        seedOrUpdateCategory("Paper Publication", "Research paper published", 25, "ALL",
                "[{\"name\":\"publicationType\",\"label\":\"Publication Type\",\"type\":\"select\",\"options\":[\"Journal\",\"Conference\",\"Book Chapter\"],\"required\":true}," +
                "{\"name\":\"doiOrIssn\",\"label\":\"DOI or ISSN\",\"type\":\"text\",\"required\":true}," +
                "{\"name\":\"authorPosition\",\"label\":\"Author Position\",\"type\":\"select\",\"options\":[\"First Author\",\"Co-Author\",\"Corresponding Author\"],\"required\":true}]");

        seedOrUpdateCategory("Internship", "Industry internship", 15, "Placement Cell",
                "[{\"name\":\"company\",\"label\":\"Company Name\",\"type\":\"text\",\"required\":true}," +
                "{\"name\":\"role\",\"label\":\"Internship Role\",\"type\":\"text\",\"required\":true}," +
                "{\"name\":\"mode\",\"label\":\"Mode\",\"type\":\"select\",\"options\":[\"Offline\",\"Online\",\"Hybrid\"],\"required\":true}," +
                "{\"name\":\"durationWeeks\",\"label\":\"Duration (Weeks)\",\"type\":\"number\",\"required\":true}," +
                "{\"name\":\"paid\",\"label\":\"Stipend Status\",\"type\":\"select\",\"options\":[\"Paid\",\"Unpaid\"],\"required\":true}]");

        seedOrUpdateCategory("Competition", "Academic competition", 12, "ALL",
                "[{\"name\":\"competitionType\",\"label\":\"Competition Type\",\"type\":\"select\",\"options\":[\"Coding\",\"Quiz\",\"Debate\",\"Project\",\"Other\"],\"required\":true}," +
                "{\"name\":\"rank\",\"label\":\"Rank / Position\",\"type\":\"text\",\"required\":true}," +
                "{\"name\":\"prize\",\"label\":\"Prize / Award\",\"type\":\"text\",\"required\":false}]");
        System.out.println("Seeded/Verified 6 categories with verifier departments");

        // 3. Seed/Verify Default Admin User
        // Reads from ADMIN_DEFAULT_PASSWORD environment variable or defaults to admin123
        String adminPass = System.getenv("ADMIN_DEFAULT_PASSWORD");
        if (adminPass == null || adminPass.trim().isEmpty()) {
            adminPass = "admin123";
        }

        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        if (adminRole != null) {
            User admin = userRepository.findByEmail("admin@anurag.edu.in").orElse(new User());
            admin.setEmail("admin@anurag.edu.in");
            admin.setPassword(passwordEncoder.encode(adminPass));
            admin.setFullName("System Administrator");
            admin.setEnabled(true);
            admin.setRole(adminRole);
            userRepository.save(admin);
            System.out.println("Seeded/Verified Admin: admin@anurag.edu.in");
        }

        // 4. Seed/Verify Default Faculty User and Faculty Profile (CSE Department)
        // Reads from FACULTY_DEFAULT_PASSWORD environment variable or defaults to faculty123
        String facultyPass = System.getenv("FACULTY_DEFAULT_PASSWORD");
        if (facultyPass == null || facultyPass.trim().isEmpty()) {
            facultyPass = "faculty123";
        }

        Role facultyRole = roleRepository.findByName("FACULTY").orElse(null);
        if (facultyRole != null) {
            User facultyUser = userRepository.findByEmail("faculty@anurag.edu.in").orElse(new User());
            facultyUser.setEmail("faculty@anurag.edu.in");
            facultyUser.setPassword(passwordEncoder.encode(facultyPass));
            facultyUser.setFullName("Dr. Ramesh Sharma");
            facultyUser.setEnabled(true);
            facultyUser.setRole(facultyRole);
            User savedFacultyUser = userRepository.save(facultyUser);

            Faculty faculty = facultyRepository.findByUser(savedFacultyUser).orElse(new Faculty());
            faculty.setDepartment("Computer Science and Engineering");
            faculty.setDesignation("Associate Professor");
            faculty.setUser(savedFacultyUser);
            facultyRepository.save(faculty);
            System.out.println("Seeded/Verified Faculty: faculty@anurag.edu.in (Dept: Computer Science and Engineering)");

            // Seed Second Faculty: Physical Education department
            User sportsFacultyUser = userRepository.findByEmail("sports_faculty@anurag.edu.in").orElse(new User());
            sportsFacultyUser.setEmail("sports_faculty@anurag.edu.in");
            sportsFacultyUser.setPassword(passwordEncoder.encode(facultyPass));
            sportsFacultyUser.setFullName("Prof. Suresh Kumar");
            sportsFacultyUser.setEnabled(true);
            sportsFacultyUser.setRole(facultyRole);
            User savedSportsUser = userRepository.save(sportsFacultyUser);

            Faculty sportsFaculty = facultyRepository.findByUser(savedSportsUser).orElse(new Faculty());
            sportsFaculty.setDepartment("Physical Education");
            sportsFaculty.setDesignation("Physical Director");
            sportsFaculty.setUser(savedSportsUser);
            facultyRepository.save(sportsFaculty);
            System.out.println("Seeded/Verified Faculty: sports_faculty@anurag.edu.in (Dept: Physical Education)");
        }

        // 5. Seed/Verify Default Student User and Student Profile
        // Login: student@anurag.edu.in | Password: student123
        Role studentRole = roleRepository.findByName("STUDENT").orElse(null);
        if (studentRole != null) {
            User studentUser = userRepository.findByEmail("student@anurag.edu.in").orElse(new User());
            studentUser.setEmail("student@anurag.edu.in");
            studentUser.setPassword(passwordEncoder.encode("student123"));
            studentUser.setFullName("Rahul Sharma");
            studentUser.setEnabled(true);
            studentUser.setRole(studentRole);
            User savedStudentUser = userRepository.save(studentUser);

            // Find existing student by roll number or by user
            Student student = studentRepository.findByRollNumber("21EG105A01")
                    .or(() -> studentRepository.findByUser(savedStudentUser))
                    .orElse(new Student());

            student.setRollNumber("21EG105A01");
            student.setBranch("CSE");
            student.setYear(3);
            student.setPhone("9876543210");
            student.setProfileSlug("21eg105a01-rahul-sharma");
            student.setVisibility(Visibility.PRIVATE);
            student.setUser(savedStudentUser);
            studentRepository.save(student);
            System.out.println("Seeded/Verified Student: student@anurag.edu.in (Password: student123)");
        }

        // Seed @college.edu counterparts for seamless login with either domain
        if (adminRole != null && userRepository.findByEmail("admin@college.edu").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@college.edu");
            admin.setPassword(passwordEncoder.encode(adminPass));
            admin.setFullName("System Administrator");
            admin.setEnabled(true);
            admin.setRole(adminRole);
            userRepository.save(admin);
            System.out.println("Seeded Admin: admin@college.edu");
        }

        if (facultyRole != null && userRepository.findByEmail("faculty@college.edu").isEmpty()) {
            User fUser = new User();
            fUser.setEmail("faculty@college.edu");
            fUser.setPassword(passwordEncoder.encode(facultyPass));
            fUser.setFullName("Dr. Ramesh Sharma");
            fUser.setEnabled(true);
            fUser.setRole(facultyRole);
            User savedF = userRepository.save(fUser);

            Faculty f = new Faculty();
            f.setDepartment("Computer Science and Engineering");
            f.setDesignation("Associate Professor");
            f.setUser(savedF);
            facultyRepository.save(f);
            System.out.println("Seeded Faculty: faculty@college.edu");
        }

        if (studentRole != null && userRepository.findByEmail("student@college.edu").isEmpty()) {
            User sUser = new User();
            sUser.setEmail("student@college.edu");
            sUser.setPassword(passwordEncoder.encode("student123"));
            sUser.setFullName("Rahul Sharma");
            sUser.setEnabled(true);
            sUser.setRole(studentRole);
            User savedS = userRepository.save(sUser);

            Student s = new Student();
            s.setRollNumber("21EG105A99");
            s.setBranch("CSE");
            s.setYear(3);
            s.setPhone("9876543210");
            s.setProfileSlug("21eg105a99-rahul-sharma");
            s.setVisibility(Visibility.PUBLIC);
            s.setUser(savedS);
            studentRepository.save(s);
            System.out.println("Seeded Student: student@college.edu");
        }

        // 6. One-time fix: ensure any existing student without a slug gets an auto-generated unique slug
        for (Student s : studentRepository.findAll()) {
            if (s.getProfileSlug() == null || s.getProfileSlug().trim().isEmpty()) {
                String sName = (s.getUser() != null && s.getUser().getFullName() != null)
                        ? s.getUser().getFullName().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "")
                        : "student";
                String sRoll = (s.getRollNumber() != null)
                        ? s.getRollNumber().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "")
                        : String.valueOf(s.getId());
                String baseSlug = sName + "-" + sRoll;
                String finalSlug = baseSlug;
                int suffix = 2;
                while (studentRepository.findByProfileSlug(finalSlug).isPresent()) {
                    finalSlug = baseSlug + "-" + suffix;
                    suffix++;
                }
                s.setProfileSlug(finalSlug);
                studentRepository.save(s);
                System.out.println("Fixed missing profileSlug for student " + s.getRollNumber() + ": " + finalSlug);
            }
        }
    }

    // helper to insert category or update verifierDepartment & fieldSchema
    private void seedOrUpdateCategory(String name, String description, int points, String verifierDepartment, String schemaJson) {
        Category category = categoryRepository.findByName(name).orElse(null);
        if (category == null) {
            category = new Category(name, description, points, verifierDepartment, schemaJson);
            categoryRepository.save(category);
        } else {
            // Update verifierDepartment if empty or old seed value
            String currentDept = category.getVerifierDepartment();
            if (currentDept == null || currentDept.trim().isEmpty()
                    || "CSE".equalsIgnoreCase(currentDept)
                    || "PHYSICAL_EDUCATION".equalsIgnoreCase(currentDept)
                    || "PLACEMENT".equalsIgnoreCase(currentDept)
                    || "TRAINING_AND_PLACEMENT".equalsIgnoreCase(currentDept)
                    || "RESEARCH".equalsIgnoreCase(currentDept)) {
                category.setVerifierDepartment(verifierDepartment);
            }
            // Update schema if empty or not a valid JSON array format
            String currentSchema = category.getFieldSchema();
            if (currentSchema == null || !currentSchema.trim().startsWith("[")) {
                category.setFieldSchema(schemaJson);
            }
            categoryRepository.save(category);
        }
    }
}
