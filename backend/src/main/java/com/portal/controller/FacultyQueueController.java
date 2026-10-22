package com.portal.controller;

import com.portal.dto.AchievementResponseDto;
import com.portal.dto.DecisionDto;
import com.portal.entity.Achievement;
import com.portal.service.AchievementService;
import com.portal.service.VerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
public class FacultyQueueController {

    private final VerificationService verificationService;
    private final AchievementService achievementService;

    public FacultyQueueController(VerificationService verificationService,
                                  AchievementService achievementService) {
        this.verificationService = verificationService;
        this.achievementService = achievementService;
    }

    // get pending verification queue for faculty with department filter
    @GetMapping("/queue")
    public ResponseEntity<List<AchievementResponseDto>> getQueue(@RequestParam(required = false) String branch,
                                                                 @RequestParam(required = false) Integer year,
                                                                 @RequestParam(required = false) Long categoryId,
                                                                 Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Achievement> queue = verificationService.getQueueForFaculty(principal.getName(), branch, year, categoryId);
        return ResponseEntity.ok(achievementService.toDtoList(queue));
    }

    // get single achievement for verification review
    @GetMapping("/achievements/{id}")
    public ResponseEntity<AchievementResponseDto> getAchievementForReview(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Achievement achievement = verificationService.getAchievementForReview(id, principal.getName());
        return ResponseEntity.ok(achievementService.toDto(achievement));
    }

    // submit verification decision (APPROVE, REJECT, SEND_BACK)
    @PostMapping("/achievements/{id}/verify")
    public ResponseEntity<Map<String, String>> verifyAchievement(@PathVariable Long id,
                                                                 @Valid @RequestBody DecisionDto dto,
                                                                 Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        verificationService.decide(id, dto, principal.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Verification decision recorded successfully.");
        return ResponseEntity.ok(response);
    }
}
