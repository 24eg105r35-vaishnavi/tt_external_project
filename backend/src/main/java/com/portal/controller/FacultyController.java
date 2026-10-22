package com.portal.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dto.DecisionDto;
import com.portal.entity.Achievement;
import com.portal.entity.Category;
import com.portal.entity.Document;
import com.portal.entity.Faculty;
import com.portal.entity.Verification;
import com.portal.repository.CategoryRepository;
import com.portal.service.AchievementService;
import com.portal.service.FileStorageService;
import com.portal.service.VerificationService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/faculty")
public class FacultyController {

    private final VerificationService verificationService;
    private final AchievementService achievementService;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    private final List<String> branches = Arrays.asList("CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "AI&DS");
    private final List<Integer> years = Arrays.asList(1, 2, 3, 4);

    public FacultyController(VerificationService verificationService,
                             AchievementService achievementService,
                             CategoryRepository categoryRepository,
                             FileStorageService fileStorageService,
                             ObjectMapper objectMapper) {
        this.verificationService = verificationService;
        this.achievementService = achievementService;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    // Faculty Queue: view pending achievements with optional branch, year, category filters
    @GetMapping("/queue")
    public String showQueue(@RequestParam(value = "branch", required = false) String branch,
                            @RequestParam(value = "year", required = false) Integer year,
                            @RequestParam(value = "categoryId", required = false) Long categoryId,
                            Principal principal,
                            Model model) {
        Faculty faculty = verificationService.getFacultyByEmail(principal.getName());

        List<Achievement> queue = verificationService.getQueueForFaculty(
                principal.getName(), branch, year, categoryId
        );

        // find categories relevant to this faculty
        List<Category> allCategories = categoryRepository.findByActiveTrue();
        List<Category> facultyCategories = new ArrayList<>();
        for (Category cat : allCategories) {
            if (verificationService.isFacultyAllowedForCategory(faculty, cat)) {
                facultyCategories.add(cat);
            }
        }

        model.addAttribute("queue", queue);
        model.addAttribute("faculty", faculty);
        model.addAttribute("branches", branches);
        model.addAttribute("years", years);
        model.addAttribute("categories", facultyCategories);
        model.addAttribute("selectedBranch", branch != null ? branch.trim() : "");
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedCategoryId", categoryId);

        return "faculty/queue";
    }

    // Review achievement claim details, proof documents, verification history, and decision form
    @GetMapping("/achievements/{id}")
    public String reviewAchievement(@PathVariable("id") Long id,
                                    Principal principal,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            Achievement achievement = verificationService.getAchievementForReview(id, principal.getName());
            List<Document> documents = achievementService.getDocumentsForAchievement(achievement);
            List<Verification> history = verificationService.getVerificationsForAchievement(achievement);

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
            model.addAttribute("extraFieldsMap", extraFieldsMap);

            if (!model.containsAttribute("decisionDto")) {
                model.addAttribute("decisionDto", new DecisionDto());
            }

            return "faculty/review";

        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/faculty/queue";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/faculty/queue";
        }
    }

    // Submit review decision (APPROVE, REJECT, or SEND_BACK)
    @PostMapping("/achievements/{id}/decision")
    public String handleDecision(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("decisionDto") DecisionDto dto,
                                 BindingResult bindingResult,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a decision action.");
            return "redirect:/faculty/achievements/" + id;
        }

        try {
            verificationService.decide(id, dto, principal.getName());
            String actionName = dto.getDecision().toUpperCase();
            if (actionName.equals("APPROVE") || actionName.equals("APPROVED")) {
                redirectAttributes.addFlashAttribute("successMessage", "Achievement approved successfully!");
            } else if (actionName.equals("REJECT") || actionName.equals("REJECTED")) {
                redirectAttributes.addFlashAttribute("successMessage", "Achievement rejected with feedback.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Achievement sent back to student for correction.");
            }
            return "redirect:/faculty/queue";

        } catch (IllegalStateException e) {
            // Already reviewed
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/faculty/queue";

        } catch (IllegalArgumentException e) {
            // Missing reason or invalid input
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/faculty/achievements/" + id;

        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/faculty/queue";
        }
    }

    // Controlled document download/preview endpoint for faculty
    @GetMapping("/documents/{id}")
    @ResponseBody
    public ResponseEntity<Resource> serveDocumentForFaculty(@PathVariable("id") Long id,
                                                            Principal principal) {
        Document document = verificationService.getDocumentForFaculty(id, principal.getName());
        Resource fileResource = fileStorageService.loadAsResource(document.getStoredName());

        String contentType = document.getFileType() != null ? document.getFileType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getOriginalName() + "\"")
                .body(fileResource);
    }
}
