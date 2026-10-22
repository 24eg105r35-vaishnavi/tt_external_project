package com.portal.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dto.AchievementDto;
import com.portal.dto.AchievementResponseDto;
import com.portal.dto.CategoryDto;
import com.portal.entity.Achievement;
import com.portal.service.AchievementService;
import com.portal.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentAchievementController {

    private final AchievementService achievementService;
    private final CategoryService categoryService;
    private final ObjectMapper objectMapper;

    public StudentAchievementController(AchievementService achievementService,
                                        CategoryService categoryService,
                                        ObjectMapper objectMapper) {
        this.achievementService = achievementService;
        this.categoryService = categoryService;
        this.objectMapper = objectMapper;
    }

    // get active categories for achievement submission
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategoryDtos());
    }

    // student dashboard stats (pending, approved, needs action, total points, recent)
    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(achievementService.getStudentDashboardStats(principal.getName()));
    }

    // list achievements for logged-in student
    @GetMapping("/achievements")
    public ResponseEntity<List<AchievementResponseDto>> getMyAchievements(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Achievement> achievements = achievementService.getAchievementsForStudent(principal.getName());
        return ResponseEntity.ok(achievementService.toDtoList(achievements));
    }

    // get single achievement detail for logged-in student
    @GetMapping("/achievements/{id}")
    public ResponseEntity<AchievementResponseDto> getAchievementDetail(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Achievement achievement = achievementService.getAchievementById(id, principal.getName());
        return ResponseEntity.ok(achievementService.toDto(achievement));
    }

    // submit a new achievement with file and extra dynamic fields
    @PostMapping(value = "/achievements", consumes = {"multipart/form-data"})
    public ResponseEntity<AchievementResponseDto> submitAchievement(@Valid @ModelAttribute AchievementDto dto,
                                                                    @RequestParam(value = "file", required = true) MultipartFile file,
                                                                    @RequestParam(value = "extraData", required = false) String extraDataJson,
                                                                    Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, String> extraFields = parseExtraDataJson(extraDataJson);
        Achievement saved = achievementService.submitAchievement(dto, file, extraFields, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(achievementService.toDto(saved));
    }

    // resubmit / edit an achievement when in NEEDS_CORRECTION or REJECTED status
    @PutMapping(value = "/achievements/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<AchievementResponseDto> resubmitAchievement(@PathVariable Long id,
                                                                      @Valid @ModelAttribute AchievementDto dto,
                                                                      @RequestParam(value = "file", required = false) MultipartFile file,
                                                                      @RequestParam(value = "extraData", required = false) String extraDataJson,
                                                                      Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, String> extraFields = parseExtraDataJson(extraDataJson);
        Achievement updated = achievementService.resubmitAchievement(id, dto, file, extraFields, principal.getName());
        return ResponseEntity.ok(achievementService.toDto(updated));
    }

    // helper to parse dynamic extraData JSON string to Map<String, String>
    private Map<String, String> parseExtraDataJson(String extraDataJson) {
        Map<String, String> extraFields = new HashMap<>();
        if (extraDataJson != null && !extraDataJson.trim().isEmpty()) {
            try {
                extraFields = objectMapper.readValue(extraDataJson, new TypeReference<Map<String, String>>() {});
            } catch (Exception ignored) {
            }
        }
        return extraFields;
    }
}
