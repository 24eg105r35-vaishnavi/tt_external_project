package com.portal.controller;

import com.portal.dto.PublicProfileDto;
import com.portal.entity.Student;
import com.portal.entity.Visibility;
import com.portal.service.ProfileService;
import com.portal.util.ProfileNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // Public profile view: accessible publicly (rule checked in ProfileService)
    @GetMapping("/u/{slug}")
    public String viewPublicProfile(@PathVariable("slug") String slug,
                                    Principal principal,
                                    Model model) {
        String viewerEmail = (principal != null) ? principal.getName() : null;
        PublicProfileDto profile = profileService.getPublicProfile(slug, viewerEmail);
        model.addAttribute("profile", profile);
        model.addAttribute("slug", slug);
        return "profile/public";
    }

    // Catch ProfileNotFoundException for private/missing profiles and show friendly 404 page
    @ExceptionHandler(ProfileNotFoundException.class)
    public String handleProfileNotFound(ProfileNotFoundException ex,
                                        HttpServletResponse response,
                                        Model model) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("errorMessage", ex.getMessage());
        return "profile/not-found";
    }

    // Student profile settings page
    @GetMapping("/student/profile")
    public String showProfileSettings(Principal principal, Model model) {
        Student student = profileService.getStudentProfile(principal.getName());
        model.addAttribute("student", student);
        model.addAttribute("visibilities", Visibility.values());
        model.addAttribute("profileUrl", "/u/" + student.getProfileSlug());
        return "student/profile";
    }

    // Save profile visibility changes
    @PostMapping("/student/profile/visibility")
    public String updateVisibility(@RequestParam("visibility") String visibilityStr,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            Visibility visibility = Visibility.valueOf(visibilityStr.trim().toUpperCase());
            profileService.updateVisibility(principal.getName(), visibility);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Profile visibility updated to " + visibility.name().replace('_', ' ') + ".");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid visibility option selected.");
        }
        return "redirect:/student/profile";
    }
}
