package com.portal.service;

import com.portal.dto.RegisterDto;
import com.portal.dto.UserDto;
import com.portal.dto.StudentProfileDto;
import com.portal.dto.FacultyDto;
import com.portal.entity.Faculty;
import com.portal.entity.Role;
import com.portal.entity.Student;
import com.portal.entity.User;
import com.portal.entity.Visibility;
import com.portal.repository.RoleRepository;
import com.portal.repository.StudentRepository;
import com.portal.repository.UserRepository;
import com.portal.util.RegistrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.allowed-domain:@college.edu}")
    private String allowedDomain;

    public UserService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerStudent(RegisterDto dto) {
        String email = dto.getEmail().trim().toLowerCase();

        // 1. check college domain
        boolean validDomain = email.endsWith(allowedDomain.toLowerCase())
                || email.endsWith("@college.edu")
                || email.endsWith("@anurag.edu.in");
        if (!validDomain) {
            throw new RegistrationException("Only college emails ending with " + allowedDomain + " are allowed.");
        }

        // 2. check passwords match
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RegistrationException("Passwords do not match.");
        }

        // 3. check duplicate email
        if (userRepository.existsByEmail(email)) {
            throw new RegistrationException("Email is already registered. Please login.");
        }

        // 4. check duplicate roll number
        String rollNumber = dto.getRollNumber().trim().toUpperCase();
        if (studentRepository.findByRollNumber(rollNumber).isPresent()) {
            throw new RegistrationException("Roll number is already registered.");
        }

        // 5. fetch the STUDENT role
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RegistrationException("STUDENT role not found in database."));

        // 6. create and save User entity (encode password once)
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName().trim());
        user.setEnabled(true);
        user.setRole(studentRole);
        User savedUser = userRepository.save(user);

        // 7. create and save Student entity
        Student student = new Student();
        student.setRollNumber(rollNumber);
        student.setBranch(dto.getBranch().trim());
        student.setYear(dto.getYear());
        student.setPhone(dto.getPhone().trim());

        // make profile slug: name-rollnumber (lowercase, letters, numbers, hyphens only)
        String uniqueSlug = generateUniqueSlug(dto.getFullName(), rollNumber);
        student.setProfileSlug(uniqueSlug);
        student.setVisibility(Visibility.PRIVATE);
        student.setUser(savedUser);

        studentRepository.save(student);
    }

    // helper to make lowercase unique slug: name-rollnumber, then name-rollnumber-2 if taken
    public String generateUniqueSlug(String fullName, String rollNumber) {
        String cleanName = fullName.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        String cleanRoll = rollNumber.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        String baseSlug = cleanName + "-" + cleanRoll;

        String uniqueSlug = baseSlug;
        int counter = 2;
        while (studentRepository.findByProfileSlug(uniqueSlug).isPresent()) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }
        return uniqueSlug;
    }

    // converter method from User entity to UserDto
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        if (user.getRole() != null) {
            dto.setRole(user.getRole().getName());
        }
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    // converter method from Student entity to StudentProfileDto
    public StudentProfileDto toStudentProfileDto(Student student, int totalPoints) {
        if (student == null) {
            return null;
        }
        StudentProfileDto dto = new StudentProfileDto();
        dto.setId(student.getId());
        dto.setRollNumber(student.getRollNumber());
        dto.setBranch(student.getBranch());
        dto.setYear(student.getYear());
        dto.setPhone(student.getPhone());
        dto.setProfileSlug(student.getProfileSlug());
        if (student.getVisibility() != null) {
            dto.setVisibility(student.getVisibility().name());
        }
        dto.setTotalPoints(totalPoints);
        dto.setUser(toDto(student.getUser()));
        return dto;
    }

    // converter method from Faculty entity to FacultyDto
    public FacultyDto toFacultyDto(Faculty faculty) {
        if (faculty == null) {
            return null;
        }
        FacultyDto dto = new FacultyDto();
        dto.setId(faculty.getId());
        dto.setDepartment(faculty.getDepartment());
        dto.setDesignation(faculty.getDesignation());
        dto.setUser(toDto(faculty.getUser()));
        return dto;
    }
}
