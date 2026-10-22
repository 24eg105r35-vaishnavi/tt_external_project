package com.portal.controller;

import com.portal.dto.CategoryDto;
import com.portal.entity.Category;
import com.portal.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryRestController {

    private final CategoryService categoryService;

    private final List<String> departments = Arrays.asList(
            "ALL", "CSE", "ECE", "EEE", "IT", "MECH", "CIVIL", "SPORTS", "EXAM_BRANCH"
    );

    public AdminCategoryRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // list all categories (both active and inactive)
    @GetMapping
    public ResponseEntity<List<CategoryDto>> listCategories() {
        return ResponseEntity.ok(categoryService.getAllCategoryDtos());
    }

    // get department options for category verifier assignment
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(departments);
    }

    // create a new category with JSON schema validation
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody Category category, Principal principal) {
        String actor = (principal != null) ? principal.getName() : "admin";
        Category saved = categoryService.saveCategory(category, actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.toDto(saved));
    }

    // update an existing category
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id,
                                                      @Valid @RequestBody Category category,
                                                      Principal principal) {
        category.setId(id);
        String actor = (principal != null) ? principal.getName() : "admin";
        Category updated = categoryService.saveCategory(category, actor);
        return ResponseEntity.ok(categoryService.toDto(updated));
    }

    // toggle active/inactive status (categories are never hard deleted)
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, String>> toggleActive(@PathVariable Long id, Principal principal) {
        String actor = (principal != null) ? principal.getName() : "admin";
        categoryService.toggleActive(id, actor);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Category status toggled successfully.");
        return ResponseEntity.ok(response);
    }
}
