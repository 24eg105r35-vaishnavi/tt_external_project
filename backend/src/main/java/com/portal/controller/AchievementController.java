package com.portal.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dto.AchievementDto;
import com.portal.entity.Achievement;
import com.portal.entity.Category;
import com.portal.entity.Document;
import com.portal.repository.CategoryRepository;
import com.portal.service.AchievementService;
import com.portal.service.FileStorageService;
import com.portal.util.FileStorageException;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.portal.entity.Status;
import com.portal.entity.Verification;
import com.portal.service.VerificationService;

@Controller
@RequestMapping("/student")
public class AchievementController {

    private final AchievementService achievementService;
    private final VerificationService verificationService;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    // standard achievement levels
    private final List<String> levels = Arrays.asList("COLLEGE", "STATE", "NATIONAL", "INTERNATIONAL");

    public AchievementController(AchievementService achievementService,
                                 VerificationService verificationService,
                                 CategoryRepository categoryRepository,
                                 FileStorageService fileStorageService,
                                 ObjectMapper objectMapper) {
        this.achievementService = achievementService;
        this.verificationService = verificationService;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    // helper to add common form attributes
    private void populateFormAttributes(Model model) {
        model.addAttribute("categories", categoryRepository.findByActiveTrue());
        model.addAttribute("levels", levels);
    }

    // show achievement upload form
    @GetMapping("/achievements/new")
    public String showUploadForm(Model model) {
        if (!model.containsAttribute("achievementDto")) {
            model.addAttribute("achievementDto", new AchievementDto());
        }
        populateFormAttributes(model);
        return "student/upload";
    }

    // return category dynamic field schema as JSON for JavaScript
    @GetMapping(value = "/achievements/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> getCategoryFields(@RequestParam("categoryId") Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null || category.getFieldSchema() == null || category.getFieldSchema().trim().isEmpty()) {
            return ResponseEntity.ok("[]");
        }
        return ResponseEntity.ok(category.getFieldSchema());
    }

    // handle achievement multipart submission
    @PostMapping("/achievements")
    public String submitAchievement(@Valid @ModelAttribute("achievementDto") AchievementDto dto,
                                    BindingResult bindingResult,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    @RequestParam Map<String, String> allParams,
                                    Principal principal,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        // extract category-specific extra fields (exclude standard form keys)
        Set<String> standardKeys = new HashSet<>(Arrays.asList(
                "categoryId", "title", "description", "level", "eventDate", "organizer", "_csrf"
        ));
        Map<String, String> extraFields = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (!standardKeys.contains(entry.getKey())) {
                extraFields.put(entry.getKey(), entry.getValue());
            }
        }

        // check bean validation errors
        if (bindingResult.hasErrors()) {
            populateFormAttributes(model);
            return "student/upload";
        }

        // check file presence
        if (file == null || file.isEmpty()) {
            model.addAttribute("fileError", "Please select a proof document (PDF, PNG, or JPG).");
            populateFormAttributes(model);
            return "student/upload";
        }

        try {
            achievementService.submitAchievement(dto, file, extraFields, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Achievement submitted successfully! It is now PENDING verification.");
            return "redirect:/student/achievements";

        } catch (FileStorageException | IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateFormAttributes(model);
            return "student/upload";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while saving your achievement: " + e.getMessage());
            populateFormAttributes(model);
            return "student/upload";
        }
    }

    // list student's own achievements
    @GetMapping("/achievements")
    public String listAchievements(Principal principal, Model model) {
        List<Achievement> achievements = achievementService.getAchievementsForStudent(principal.getName());
        model.addAttribute("achievements", achievements);
        return "student/list";
    }

    // achievement detail page
    @GetMapping("/achievements/{id}")
    public String viewAchievementDetail(@PathVariable("id") Long id,
                                        Principal principal,
                                        Model model) {
        try {
            Achievement achievement = achievementService.getAchievementById(id, principal.getName());
            List<Document> documents = achievementService.getDocumentsForAchievement(achievement);
            List<Verification> history = verificationService.getVerificationsForAchievement(achievement);

            // get the latest verification reason if rejected or needs correction
            String latestReason = null;
            if (!history.isEmpty()) {
                latestReason = history.get(0).getReason();
            }

            // parse extraData JSON if present
            Map<String, Object> extraFieldsMap = new HashMap<>();
            if (achievement.getExtraData() != null && !achievement.getExtraData().trim().isEmpty()) {
                try {
                    extraFieldsMap = objectMapper.readValue(
                            achievement.getExtraData(),
                            new TypeReference<Map<String, Object>>() {}
                    );
                } catch (Exception ignored) {
                }
            }

            model.addAttribute("achievement", achievement);
            model.addAttribute("documents", documents);
            model.addAttribute("history", history);
            model.addAttribute("latestReason", latestReason);
            model.addAttribute("extraFieldsMap", extraFieldsMap);
            return "student/detail";

        } catch (SecurityException e) {
            return "redirect:/student/achievements?error=unauthorized";
        }
    }

    // show edit / resubmission form (only allowed for REJECTED or NEEDS_CORRECTION)
    @GetMapping("/achievements/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id,
                               Principal principal,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            Achievement achievement = achievementService.getAchievementById(id, principal.getName());

            // check allowed status
            if (achievement.getStatus() != Status.REJECTED && achievement.getStatus() != Status.NEEDS_CORRECTION) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Cannot edit achievement in " + achievement.getStatus() + " status. Only REJECTED or NEEDS_CORRECTION items can be edited.");
                return "redirect:/student/achievements/" + id;
            }

            AchievementDto dto = new AchievementDto();
            dto.setCategoryId(achievement.getCategory().getId());
            dto.setTitle(achievement.getTitle());
            dto.setDescription(achievement.getDescription());
            dto.setLevel(achievement.getLevel());
            dto.setEventDate(achievement.getEventDate());
            dto.setOrganizer(achievement.getOrganizer());

            // parse extraData
            Map<String, Object> extraFieldsMap = new HashMap<>();
            if (achievement.getExtraData() != null && !achievement.getExtraData().trim().isEmpty()) {
                try {
                    extraFieldsMap = objectMapper.readValue(
                            achievement.getExtraData(),
                            new TypeReference<Map<String, Object>>() {}
                    );
                } catch (Exception ignored) {}
            }

            List<Document> documents = achievementService.getDocumentsForAchievement(achievement);
            List<Verification> history = verificationService.getVerificationsForAchievement(achievement);

            model.addAttribute("achievement", achievement);
            model.addAttribute("achievementDto", dto);
            model.addAttribute("documents", documents);
            model.addAttribute("history", history);
            model.addAttribute("extraFieldsMap", extraFieldsMap);
            populateFormAttributes(model);

            return "student/edit";

        } catch (SecurityException e) {
            return "redirect:/student/achievements?error=unauthorized";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/achievements";
        }
    }

    // handle resubmission POST
    @PostMapping("/achievements/{id}/edit")
    public String resubmitAchievement(@PathVariable("id") Long id,
                                      @Valid @ModelAttribute("achievementDto") AchievementDto dto,
                                      BindingResult bindingResult,
                                      @RequestParam(value = "file", required = false) MultipartFile file,
                                      @RequestParam Map<String, String> allParams,
                                      Principal principal,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        // extract extra fields
        Set<String> standardKeys = new HashSet<>(Arrays.asList(
                "categoryId", "title", "description", "level", "eventDate", "organizer", "_csrf"
        ));
        Map<String, String> extraFields = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (!standardKeys.contains(entry.getKey())) {
                extraFields.put(entry.getKey(), entry.getValue());
            }
        }

        if (bindingResult.hasErrors()) {
            Achievement achievement = achievementService.getAchievementById(id, principal.getName());
            model.addAttribute("achievement", achievement);
            model.addAttribute("documents", achievementService.getDocumentsForAchievement(achievement));
            model.addAttribute("history", verificationService.getVerificationsForAchievement(achievement));
            populateFormAttributes(model);
            return "student/edit";
        }

        try {
            achievementService.resubmitAchievement(id, dto, file, extraFields, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Achievement resubmitted successfully! It is now PENDING review.");
            return "redirect:/student/achievements/" + id;

        } catch (IllegalStateException | IllegalArgumentException | FileStorageException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/achievements/" + id + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error resubmitting achievement: " + e.getMessage());
            return "redirect:/student/achievements/" + id + "/edit";
        }
    }

    // serve proof document with security ownership check
    @GetMapping("/documents/{id}")
    @ResponseBody
    public ResponseEntity<Resource> serveDocument(@PathVariable("id") Long id,
                                                  Principal principal) {
        Document document = achievementService.getDocumentById(id, principal.getName());
        Resource fileResource = fileStorageService.loadAsResource(document.getStoredName());

        String contentType = document.getFileType() != null ? document.getFileType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getOriginalName() + "\"")
                .body(fileResource);
    }
}
