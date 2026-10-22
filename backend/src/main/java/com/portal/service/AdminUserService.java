package com.portal.service;

import com.portal.dto.FacultyCreateDto;
import com.portal.entity.AuditLog;
import com.portal.entity.Faculty;
import com.portal.entity.Role;
import com.portal.entity.User;
import com.portal.repository.AuditLogRepository;
import com.portal.repository.FacultyRepository;
import com.portal.repository.RoleRepository;
import com.portal.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final RoleRepository roleRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository,
                            FacultyRepository facultyRepository,
                            RoleRepository roleRepository,
                            AuditLogRepository auditLogRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
        this.roleRepository = roleRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // list users with keyword filter and paging (10 per page)
    @Transactional(readOnly = true)
    public Page<User> listUsers(String keyword, int page) {
        if (page < 0) {
            page = 0;
        }
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return userRepository.searchUsers(cleanKeyword, pageable);
    }

    // create a new FACULTY user in one transaction
    @Transactional
    public User createFacultyUser(FacultyCreateDto dto, String adminEmail) {
        String email = dto.getEmail().trim().toLowerCase();

        // 1. Check duplicate email
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with email '" + email + "' already exists.");
        }

        // 2. Fetch FACULTY role
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseThrow(() -> new IllegalArgumentException("FACULTY role not found in database."));

        // 3. Create User account with BCrypt encoded password
        User user = new User();
        user.setEmail(email);
        user.setFullName(dto.getFullName().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(true);
        user.setRole(facultyRole);
        User savedUser = userRepository.save(user);

        // 4. Create Faculty profile
        Faculty faculty = new Faculty();
        faculty.setDepartment(dto.getDepartment().trim());
        faculty.setDesignation(dto.getDesignation().trim());
        faculty.setUser(savedUser);
        facultyRepository.save(faculty);

        // 5. Write AuditLog
        AuditLog log = new AuditLog();
        log.setAction("USER_CREATED");
        log.setEntityName("User");
        log.setEntityId(savedUser.getId());
        log.setNewValue("email=" + savedUser.getEmail() + ", role=FACULTY, dept=" + dto.getDepartment());
        log.setPerformedBy(adminEmail);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);

        return savedUser;
    }

    // toggle user enabled/disabled status (prevent self-disable)
    @Transactional
    public void toggleUserStatus(Long userId, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Admin cannot disable their own account
        if (user.getEmail().equalsIgnoreCase(adminEmail.trim())) {
            throw new IllegalArgumentException("You cannot disable your own admin account.");
        }

        boolean oldStatus = user.isEnabled();
        user.setEnabled(!oldStatus);
        userRepository.save(user);

        // Write AuditLog
        AuditLog log = new AuditLog();
        log.setAction(user.isEnabled() ? "USER_ENABLED" : "USER_DISABLED");
        log.setEntityName("User");
        log.setEntityId(user.getId());
        log.setOldValue("enabled=" + oldStatus);
        log.setNewValue("enabled=" + user.isEnabled());
        log.setPerformedBy(adminEmail);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
