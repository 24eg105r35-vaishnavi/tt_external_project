package com.portal.controller;

import com.portal.entity.AuditLog;
import com.portal.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/audit")
public class AdminAuditController {

    private final AuditLogRepository auditLogRepository;

    private final List<String> commonActions = Arrays.asList(
            "LOGIN", "LOGOUT", "ACHIEVEMENT_UPLOAD", "ACHIEVEMENT_RESUBMIT",
            "ACHIEVEMENT_APPROVED", "ACHIEVEMENT_REJECTED", "ACHIEVEMENT_NEEDS_CORRECTION",
            "PROFILE_VISIBILITY_CHANGED", "EXPORT_CSV", "CATEGORY_CREATED", "CATEGORY_UPDATED",
            "USER_CREATED", "USER_ENABLED", "USER_DISABLED"
    );

    public AdminAuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // View audit logs with optional action/user filter and paging (read-only)
    @GetMapping
    public String viewAuditLogs(@RequestParam(value = "action", required = false) String action,
                                @RequestParam(value = "performedBy", required = false) String performedBy,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                Model model) {
        if (page < 0) {
            page = 0;
        }

        String cleanAction = (action != null && !action.trim().isEmpty()) ? action.trim() : null;
        String cleanUser = (performedBy != null && !performedBy.trim().isEmpty()) ? performedBy.trim() : null;

        Pageable pageable = PageRequest.of(page, 15);
        Page<AuditLog> auditPage = auditLogRepository.searchAuditLogs(cleanAction, cleanUser, pageable);

        model.addAttribute("auditPage", auditPage);
        model.addAttribute("action", action);
        model.addAttribute("performedBy", performedBy);
        model.addAttribute("commonActions", commonActions);
        model.addAttribute("currentPage", page);

        return "admin/audit";
    }
}
