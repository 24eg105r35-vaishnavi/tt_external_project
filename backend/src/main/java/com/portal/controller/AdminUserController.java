package com.portal.controller;

import com.portal.dto.FacultyCreateDto;
import com.portal.entity.User;
import com.portal.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    private final List<String> departments = Arrays.asList(
            "CSE", "ECE", "EEE", "IT", "MECH", "CIVIL", "PHYSICAL_EDUCATION", "EXAM_BRANCH"
    );

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // List all users with optional keyword search and paging
    @GetMapping
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            Model model) {
        Page<User> usersPage = adminUserService.listUsers(keyword, page);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);

        return "admin/users";
    }

    // Show form to create a new faculty user
    @GetMapping("/new-faculty")
    public String showFacultyCreateForm(Model model) {
        model.addAttribute("facultyDto", new FacultyCreateDto());
        model.addAttribute("departments", departments);
        return "admin/user-form";
    }

    // Handle faculty user creation
    @PostMapping("/faculty")
    public String createFacultyUser(@ModelAttribute("facultyDto") FacultyCreateDto dto,
                                    Principal principal,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            String adminEmail = (principal != null) ? principal.getName() : "admin";
            adminUserService.createFacultyUser(dto, adminEmail);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Faculty user '" + dto.getEmail() + "' created successfully.");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("facultyDto", dto);
            model.addAttribute("departments", departments);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/user-form";
        }
    }

    // Toggle user enabled/disabled status
    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable("id") Long id,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            String adminEmail = (principal != null) ? principal.getName() : "admin";
            adminUserService.toggleUserStatus(id, adminEmail);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
