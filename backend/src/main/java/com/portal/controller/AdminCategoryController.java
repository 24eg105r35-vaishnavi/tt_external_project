package com.portal.controller;

import com.portal.entity.Category;
import com.portal.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    private final List<String> departments = Arrays.asList(
            "ALL", "CSE", "ECE", "EEE", "IT", "MECH", "CIVIL", "SPORTS", "EXAM_BRANCH"
    );

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // List all categories (active and inactive)
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    // Show form to add a new category
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Category category = new Category();
        category.setActive(true);
        category.setVerifierDepartment("ALL");
        category.setPoints(10);
        category.setFieldSchema("[]");

        model.addAttribute("category", category);
        model.addAttribute("departments", departments);
        model.addAttribute("isNew", true);
        return "admin/category-form";
    }

    // Show form to edit an existing category
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id);

        model.addAttribute("category", category);
        model.addAttribute("departments", departments);
        model.addAttribute("isNew", false);
        return "admin/category-form";
    }

    // Save category (add or update)
    @PostMapping
    public String saveCategory(@ModelAttribute("category") Category category,
                               Principal principal,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            String userEmail = (principal != null) ? principal.getName() : "admin";
            categoryService.saveCategory(category, userEmail);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Category '" + category.getName() + "' saved successfully.");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException e) {
            model.addAttribute("category", category);
            model.addAttribute("departments", departments);
            model.addAttribute("isNew", (category.getId() == null));
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/category-form";
        }
    }

    // Toggle active status (never delete categories)
    @PostMapping("/{id}/toggle-active")
    public String toggleCategoryActive(@PathVariable("id") Long id,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        String userEmail = (principal != null) ? principal.getName() : "admin";
        categoryService.toggleActive(id, userEmail);
        redirectAttributes.addFlashAttribute("successMessage", "Category status updated.");
        return "redirect:/admin/categories";
    }
}
