package com.portal.controller;

import com.portal.dto.AuditLogDto;
import com.portal.entity.AuditLog;
import com.portal.repository.AuditLogRepository;
import com.portal.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditRestController {

    private final AuditLogRepository auditLogRepository;
    private final AdminUserService adminUserService;

    public AdminAuditRestController(AuditLogRepository auditLogRepository,
                                    AdminUserService adminUserService) {
        this.auditLogRepository = auditLogRepository;
        this.adminUserService = adminUserService;
    }

    // view audit trail with pagination (15 per page, newest first)
    @GetMapping
    public ResponseEntity<Map<String, Object>> listAuditLogs(@RequestParam(defaultValue = "0") int page) {
        if (page < 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, 15, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        Page<AuditLog> logPage = auditLogRepository.findAll(pageable);

        List<AuditLogDto> dtoList = new ArrayList<>();
        for (AuditLog log : logPage.getContent()) {
            dtoList.add(adminUserService.toAuditLogDto(log));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("content", dtoList);
        response.put("totalElements", logPage.getTotalElements());
        response.put("totalPages", logPage.getTotalPages());
        response.put("number", logPage.getNumber());
        response.put("size", logPage.getSize());

        return ResponseEntity.ok(response);
    }
}
