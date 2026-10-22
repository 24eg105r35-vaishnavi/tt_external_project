package com.portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dto.CategoryDto;
import com.portal.entity.AuditLog;
import com.portal.entity.Category;
import com.portal.repository.AuditLogRepository;
import com.portal.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public CategoryService(CategoryRepository categoryRepository,
                           AuditLogRepository auditLogRepository) {
        this.categoryRepository = categoryRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // converter method from Category entity to CategoryDto
    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setPoints(category.getPoints());
        dto.setActive(category.isActive());
        dto.setVerifierDepartment(category.getVerifierDepartment());
        dto.setFieldSchema(category.getFieldSchema());
        return dto;
    }

    public List<CategoryDto> getAllCategoryDtos() {
        List<Category> list = categoryRepository.findAll();
        List<CategoryDto> dtoList = new ArrayList<>();
        for (Category c : list) {
            dtoList.add(toDto(c));
        }
        return dtoList;
    }

    public List<CategoryDto> getActiveCategoryDtos() {
        List<Category> list = categoryRepository.findByActiveTrue();
        List<CategoryDto> dtoList = new ArrayList<>();
        for (Category c : list) {
            dtoList.add(toDto(c));
        }
        return dtoList;
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    // validate JSON string using Jackson
    public boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return true;
        }
        try {
            objectMapper.readTree(jsonString.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // create or update category with validation and audit logging
    @Transactional
    public Category saveCategory(Category category, String userEmail) {
        // 1. JSON schema validation
        String schema = category.getFieldSchema();
        if (schema != null && !schema.trim().isEmpty()) {
            if (!isValidJson(schema)) {
                throw new IllegalArgumentException("Field schema must be valid JSON format (e.g., [{\"name\":\"rank\",\"type\":\"text\",\"label\":\"Rank\"}]).");
            }
        }

        // 2. Create vs Update
        if (category.getId() == null) {
            // Check unique name
            if (categoryRepository.findByName(category.getName().trim()).isPresent()) {
                throw new IllegalArgumentException("A category with name '" + category.getName().trim() + "' already exists.");
            }

            category.setName(category.getName().trim());
            Category saved = categoryRepository.save(category);

            // Audit log for create
            AuditLog log = new AuditLog();
            log.setAction("CATEGORY_CREATED");
            log.setEntityName("Category");
            log.setEntityId(saved.getId());
            log.setNewValue("name=" + saved.getName() + ", points=" + saved.getPoints() + ", dept=" + saved.getVerifierDepartment() + ", active=" + saved.isActive());
            log.setPerformedBy(userEmail);
            log.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(log);

            return saved;
        } else {
            Category existing = getCategoryById(category.getId());

            // Check if name was changed to an existing name
            if (!existing.getName().equalsIgnoreCase(category.getName().trim())) {
                if (categoryRepository.findByName(category.getName().trim()).isPresent()) {
                    throw new IllegalArgumentException("A category with name '" + category.getName().trim() + "' already exists.");
                }
            }

            String oldValue = "name=" + existing.getName() + ", points=" + existing.getPoints() + ", dept=" + existing.getVerifierDepartment() + ", active=" + existing.isActive();

            existing.setName(category.getName().trim());
            existing.setDescription(category.getDescription());
            existing.setPoints(category.getPoints());
            existing.setVerifierDepartment(category.getVerifierDepartment());
            existing.setFieldSchema(category.getFieldSchema());
            existing.setActive(category.isActive());

            Category updated = categoryRepository.save(existing);

            String newValue = "name=" + updated.getName() + ", points=" + updated.getPoints() + ", dept=" + updated.getVerifierDepartment() + ", active=" + updated.isActive();

            // Audit log for update
            AuditLog log = new AuditLog();
            log.setAction("CATEGORY_UPDATED");
            log.setEntityName("Category");
            log.setEntityId(updated.getId());
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setPerformedBy(userEmail);
            log.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(log);

            return updated;
        }
    }

    // toggle active on/off (never delete categories)
    @Transactional
    public void toggleActive(Long id, String userEmail) {
        Category category = getCategoryById(id);
        boolean oldActive = category.isActive();
        category.setActive(!oldActive);
        categoryRepository.save(category);

        // Audit log
        AuditLog log = new AuditLog();
        log.setAction("CATEGORY_UPDATED");
        log.setEntityName("Category");
        log.setEntityId(category.getId());
        log.setOldValue("active=" + oldActive);
        log.setNewValue("active=" + category.isActive());
        log.setPerformedBy(userEmail);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
