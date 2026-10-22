package com.portal;

import com.portal.dto.CategoryDto;
import com.portal.dto.UserDto;
import com.portal.entity.Category;
import com.portal.entity.Role;
import com.portal.entity.User;
import com.portal.service.CategoryService;
import com.portal.service.UserService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DtoConverterTest {

    @Test
    public void testUserDtoConversionHidesPassword() {
        UserService userService = new UserService(null, null, null, null);

        Role role = new Role();
        role.setName("STUDENT");

        User user = new User();
        user.setId(10L);
        user.setEmail("student@anurag.edu.in");
        user.setPassword("secret_hashed_password");
        user.setFullName("Student One");
        user.setEnabled(true);
        user.setRole(role);

        UserDto dto = userService.toDto(user);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("student@anurag.edu.in", dto.getEmail());
        assertEquals("Student One", dto.getFullName());
        assertEquals("STUDENT", dto.getRole());
        assertTrue(dto.isEnabled());
    }

    @Test
    public void testCategoryDtoConversion() {
        CategoryService categoryService = new CategoryService(null, null);

        Category category = new Category();
        category.setId(1L);
        category.setName("Hackathon");
        category.setDescription("Tech hackathons");
        category.setPoints(20);
        category.setActive(true);
        category.setVerifierDepartment("Computer Science and Engineering");
        category.setFieldSchema("[{\"name\":\"rank\",\"type\":\"text\"}]");

        CategoryDto dto = categoryService.toDto(category);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Hackathon", dto.getName());
        assertEquals("Tech hackathons", dto.getDescription());
        assertEquals(20, dto.getPoints());
        assertTrue(dto.isActive());
        assertEquals("Computer Science and Engineering", dto.getVerifierDepartment());
        assertEquals("[{\"name\":\"rank\",\"type\":\"text\"}]", dto.getFieldSchema());
    }
}
