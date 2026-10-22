package com.portal.controller;

import com.portal.entity.User;
import com.portal.repository.UserRepository;
import com.portal.entity.Achievement;
import com.portal.service.AchievementService;
import com.portal.service.VerificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final AchievementService achievementService;
    private final VerificationService verificationService;

    public DashboardController(UserRepository userRepository,
                               AchievementService achievementService,
                               VerificationService verificationService) {
        this.userRepository = userRepository;
        this.achievementService = achievementService;
        this.verificationService = verificationService;
    }

    // helper method to populate user info
    private void populateUserInfo(Principal principal, Model model, String roleName) {
        if (principal != null) {
            String email = principal.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                model.addAttribute("fullName", user.getFullName());
                model.addAttribute("email", user.getEmail());
            } else {
                model.addAttribute("fullName", email);
                model.addAttribute("email", email);
            }
        }
        model.addAttribute("role", roleName);
    }

    // Student Dashboard: show counts, total points, and 5 latest achievements
    @GetMapping("/student/dashboard")
    public String studentDashboard(Principal principal, Model model) {
        populateUserInfo(principal, model, "STUDENT");
        Map<String, Object> stats = achievementService.getStudentDashboardStats(principal.getName());
        model.addAllAttributes(stats);
        return "student/dashboard";
    }

    // Faculty Dashboard: show pending queue count
    @GetMapping("/faculty/dashboard")
    public String facultyDashboard(Principal principal, Model model) {
        populateUserInfo(principal, model, "FACULTY");
        List<Achievement> queue = verificationService.getQueueForFaculty(principal.getName(), null, null, null);
        model.addAttribute("pendingCount", queue.size());
        return "faculty/dashboard";
    }

    // Admin Dashboard
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Principal principal, Model model) {
        populateUserInfo(principal, model, "ADMIN");
        return "admin/dashboard";
    }
}
