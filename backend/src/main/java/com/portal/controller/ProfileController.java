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
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ProfileController(ProfileService profileService,
                             com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.profileService = profileService;
        this.objectMapper = objectMapper;
    }

    // Public profile view: accessible publicly (rule checked in ProfileService)
    @GetMapping("/u/{slug}")
    public String viewPublicProfile(@PathVariable("slug") String slug,
                                    Principal principal,
                                    Model model) {
        String viewerEmail = (principal != null) ? principal.getName() : null;
        PublicProfileDto profile = profileService.getPublicProfile(slug, viewerEmail);

        String title = profile.getFullName() + " - " + profile.getBranch() + " - Anurag University | Verified Achievements";
        String description = "Verified achievements portfolio of " + profile.getFullName() + ", " + profile.getBranch() + " at Anurag University.";
        String cleanBase = (baseUrl != null) ? baseUrl.replaceAll("/$", "") : "http://localhost:8080";
        String canonicalUrl = cleanBase + "/u/" + slug;

        model.addAttribute("profile", profile);
        model.addAttribute("slug", slug);
        model.addAttribute("pageTitle", title);
        model.addAttribute("metaDescription", description);
        model.addAttribute("ogTitle", title);
        model.addAttribute("ogDescription", description);
        model.addAttribute("ogType", "profile");
        model.addAttribute("canonicalUrl", canonicalUrl);

        if (profile.getVisibility() == Visibility.PUBLIC) {
            model.addAttribute("metaRobots", "index, follow");
        } else {
            model.addAttribute("metaRobots", "noindex");
        }

        // JSON-LD script of type Person with verified achievements listed
        try {
            java.util.Map<String, Object> personJson = new java.util.LinkedHashMap<>();
            personJson.put("@context", "https://schema.org");
            personJson.put("@type", "Person");
            personJson.put("name", profile.getFullName());
            personJson.put("affiliation", "Anurag University");
            personJson.put("jobTitle", "Student - " + profile.getBranch());
            personJson.put("url", canonicalUrl);

            java.util.List<java.util.Map<String, Object>> awards = new java.util.ArrayList<>();
            if (profile.getGroupedAchievements() != null) {
                for (java.util.List<com.portal.dto.PublicAchievementDto> list : profile.getGroupedAchievements().values()) {
                    for (com.portal.dto.PublicAchievementDto item : list) {
                        java.util.Map<String, Object> award = new java.util.LinkedHashMap<>();
                        award.put("@type", "Achievement");
                        award.put("name", item.getTitle());
                        award.put("category", item.getCategoryName());
                        award.put("level", item.getLevel());
                        if (item.getEventDate() != null) {
                            award.put("dateCreated", item.getEventDate().toString());
                        }
                        awards.add(award);
                    }
                }
            }
            personJson.put("hasCredential", awards);
            String jsonLd = objectMapper.writeValueAsString(personJson);
            model.addAttribute("jsonLd", jsonLd);
        } catch (Exception ignored) {
        }

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
