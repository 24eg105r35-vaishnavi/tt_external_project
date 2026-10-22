package com.portal.controller;

import com.portal.dto.AchievementResponseDto;
import com.portal.dto.CategoryDto;
import com.portal.dto.SearchDto;
import com.portal.entity.Achievement;
import com.portal.service.AchievementService;
import com.portal.service.CategoryService;
import com.portal.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchRestController {

    private final SearchService searchService;
    private final AchievementService achievementService;
    private final CategoryService categoryService;

    private final List<String> branches = Arrays.asList("CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "AI&DS");
    private final List<String> levels = Arrays.asList("COLLEGE", "STATE", "NATIONAL", "INTERNATIONAL");
    private final List<Integer> years = Arrays.asList(1, 2, 3, 4);

    public SearchRestController(SearchService searchService,
                                AchievementService achievementService,
                                CategoryService categoryService) {
        this.searchService = searchService;
        this.achievementService = achievementService;
        this.categoryService = categoryService;
    }

    // search approved achievements with pagination and filters
    @GetMapping
    public ResponseEntity<Map<String, Object>> searchAchievements(@ModelAttribute SearchDto dto,
                                                                 @RequestParam(defaultValue = "0") int page) {
        Page<Achievement> resultPage = searchService.search(dto, page);

        List<AchievementResponseDto> dtoList = achievementService.toDtoList(resultPage.getContent());

        Map<String, Object> response = new HashMap<>();
        response.put("content", dtoList);
        response.put("totalElements", resultPage.getTotalElements());
        response.put("totalPages", resultPage.getTotalPages());
        response.put("number", resultPage.getNumber());
        response.put("size", resultPage.getSize());

        return ResponseEntity.ok(response);
    }

    // get filter options (branches, levels, years, categories)
    @GetMapping("/filters")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("branches", branches);
        filters.put("levels", levels);
        filters.put("years", years);
        filters.put("categories", categoryService.getActiveCategoryDtos());
        return ResponseEntity.ok(filters);
    }
}
