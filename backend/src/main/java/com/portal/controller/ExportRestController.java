package com.portal.controller;

import com.portal.dto.SearchDto;
import com.portal.entity.Achievement;
import com.portal.entity.AuditLog;
import com.portal.repository.AuditLogRepository;
import com.portal.service.ExportService;
import com.portal.service.SearchService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/export")
public class ExportRestController {

    private final SearchService searchService;
    private final ExportService exportService;
    private final AuditLogRepository auditLogRepository;

    public ExportRestController(SearchService searchService,
                                ExportService exportService,
                                AuditLogRepository auditLogRepository) {
        this.searchService = searchService;
        this.exportService = exportService;
        this.auditLogRepository = auditLogRepository;
    }

    // export filtered achievements as CSV with formula injection defense and UTF-8 BOM
    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv(@ModelAttribute SearchDto searchDto, Principal principal) {
        List<Achievement> achievements = searchService.getExportList(searchDto);
        byte[] csvData = exportService.generateAchievementsCsv(achievements);

        String actor = (principal != null) ? principal.getName() : "anonymousUser";

        // Audit log for export
        AuditLog log = new AuditLog();
        log.setAction("CSV_EXPORT");
        log.setEntityName("Achievement");
        log.setEntityId(null);
        log.setNewValue("Exported " + achievements.size() + " achievements");
        log.setPerformedBy(actor);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"achievements_export.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
