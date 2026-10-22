package com.portal.controller;

import com.portal.dto.FacultyCreateDto;
import com.portal.dto.UserDto;
import com.portal.entity.User;
import com.portal.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserRestController {

    private final AdminUserService adminUserService;

    private final List<String> departments = Arrays.asList(
            "CSE", "ECE", "EEE", "IT", "MECH", "CIVIL", "SPORTS", "EXAM_BRANCH"
    );

    public AdminUserRestController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // list users with pagination and keyword search
    @GetMapping
    public ResponseEntity<Map<String, Object>> listUsers(@RequestParam(required = false) String keyword,
                                                         @RequestParam(defaultValue = "0") int page) {
        Page<User> userPage = adminUserService.listUsers(keyword, page);

        List<UserDto> dtoList = new ArrayList<>();
        for (User u : userPage.getContent()) {
            dtoList.add(adminUserService.toUserDto(u));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("content", dtoList);
        response.put("totalElements", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        response.put("number", userPage.getNumber());
        response.put("size", userPage.getSize());

        return ResponseEntity.ok(response);
    }

    // get departments for faculty registration
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(departments);
    }

    // register a new faculty user
    @PostMapping("/faculty")
    public ResponseEntity<UserDto> createFaculty(@Valid @RequestBody FacultyCreateDto dto, Principal principal) {
        String actor = (principal != null) ? principal.getName() : "admin";
        User created = adminUserService.createFacultyUser(dto, actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.toUserDto(created));
    }

    // toggle user enabled/disabled status
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, String>> toggleStatus(@PathVariable Long id, Principal principal) {
        String actor = (principal != null) ? principal.getName() : "admin";
        adminUserService.toggleUserStatus(id, actor);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User status toggled successfully.");
        return ResponseEntity.ok(response);
    }
}
