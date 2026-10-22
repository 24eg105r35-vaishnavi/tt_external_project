package com.portal.controller;

import com.portal.entity.User;
import com.portal.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class DashboardController {

    private final UserRepository userRepository;

    public DashboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    // Student Dashboard
    @GetMapping("/student/dashboard")
    public String studentDashboard(Principal principal, Model model) {
        populateUserInfo(principal, model, "STUDENT");
        return "student/dashboard";
    }

    // Faculty Dashboard
    @GetMapping("/faculty/dashboard")
    public String facultyDashboard(Principal principal, Model model) {
        populateUserInfo(principal, model, "FACULTY");
        return "faculty/dashboard";
    }

    // Admin Dashboard
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Principal principal, Model model) {
        populateUserInfo(principal, model, "ADMIN");
        return "admin/dashboard";
    }
}
