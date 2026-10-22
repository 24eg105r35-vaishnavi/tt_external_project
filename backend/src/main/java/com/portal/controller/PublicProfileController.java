package com.portal.controller;

import com.portal.dto.PublicProfileDto;
import com.portal.dto.StudentProfileDto;
import com.portal.entity.Achievement;
import com.portal.entity.Status;
import com.portal.entity.Student;
import com.portal.entity.Visibility;
import com.portal.repository.AchievementRepository;
import com.portal.service.ProfileService;
import com.portal.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicProfileController {

    private final ProfileService profileService;
    private final UserService userService;
    private final AchievementRepository achievementRepository;

    public PublicProfileController(ProfileService profileService,
                                   UserService userService,
                                   AchievementRepository achievementRepository) {
        this.profileService = profileService;
        this.userService = userService;
        this.achievementRepository = achievementRepository;
    }

    // public portfolio by student slug (no credentials required unless visibility rules apply)
    @GetMapping("/public/profiles/{slug}")
    public ResponseEntity<PublicProfileDto> getPublicProfile(@PathVariable String slug, Principal principal) {
        String viewerEmail = (principal != null) ? principal.getName() : null;
        PublicProfileDto profile = profileService.getPublicProfile(slug, viewerEmail);
        return ResponseEntity.ok(profile);
    }

    // student private profile details for settings page
    @GetMapping("/student/profile")
    public ResponseEntity<StudentProfileDto> getMyProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Student student = profileService.getStudentProfile(principal.getName());
        int totalPoints = 0;
        List<Achievement> approved = achievementRepository.findApprovedAchievementsForProfile(student, Status.APPROVED);
        for (Achievement a : approved) {
            totalPoints += a.getPointsAwarded();
        }
        return ResponseEntity.ok(userService.toStudentProfileDto(student, totalPoints));
    }

    // update profile visibility (PUBLIC, COLLEGE_ONLY, PRIVATE)
    @PutMapping("/student/profile/visibility")
    public ResponseEntity<StudentProfileDto> updateVisibility(@RequestBody Map<String, String> body, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String visibilityStr = body.get("visibility");
        if (visibilityStr == null || visibilityStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Visibility is required.");
        }
        Visibility visibility = Visibility.valueOf(visibilityStr.trim().toUpperCase());
        profileService.updateVisibility(principal.getName(), visibility);

        Student student = profileService.getStudentProfile(principal.getName());
        int totalPoints = 0;
        List<Achievement> approved = achievementRepository.findApprovedAchievementsForProfile(student, Status.APPROVED);
        for (Achievement a : approved) {
            totalPoints += a.getPointsAwarded();
        }
        return ResponseEntity.ok(userService.toStudentProfileDto(student, totalPoints));
    }
}
