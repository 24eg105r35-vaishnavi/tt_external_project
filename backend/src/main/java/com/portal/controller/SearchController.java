package com.portal.controller;

import com.portal.dto.SearchDto;
import com.portal.entity.Achievement;
import com.portal.entity.AuditLog;
import com.portal.repository.AuditLogRepository;
import com.portal.repository.CategoryRepository;
import com.portal.service.ExportService;
import com.portal.service.SearchService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class SearchController {

    private final SearchService searchService;
    private final ExportService exportService;
    private final CategoryRepository categoryRepository;
    private final AuditLogRepository auditLogRepository;

    private final List<String> branches = Arrays.asList("CSE", "ECE", "EEE", "IT", "MECH", "CIVIL");
    private final List<Integer> years = Arrays.asList(1, 2, 3, 4);
    private final List<String> levels = Arrays.asList("COLLEGE", "STATE", "NATIONAL", "INTERNATIONAL");

    public SearchController(SearchService searchService,
                            ExportService exportService,
                            CategoryRepository categoryRepository,
                            AuditLogRepository auditLogRepository) {
        this.searchService = searchService;
        this.exportService = exportService;
        this.categoryRepository = categoryRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // College-wide search page with filter criteria and paging
    @GetMapping("/search")
    public String showSearchPage(@ModelAttribute("searchDto") SearchDto searchDto,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 Model model) {
        Page<Achievement> results = searchService.search(searchDto, page);

        model.addAttribute("results", results);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("branches", branches);
        model.addAttribute("years", years);
        model.addAttribute("levels", levels);
        model.addAttribute("currentPage", page);

        return "search/search";
    }

    // Export matching achievements to CSV with formula injection defense and BOM
    @GetMapping("/export/csv")
    public void exportToCsv(@ModelAttribute("searchDto") SearchDto searchDto,
                            Principal principal,
                            HttpServletResponse response) throws IOException {
        List<Achievement> achievements = searchService.getExportList(searchDto);
        byte[] csvData = exportService.generateAchievementsCsv(achievements);

        String userEmail = (principal != null) ? principal.getName() : "system";

        // Log EXPORT_CSV audit record
        AuditLog log = new AuditLog();
        log.setAction("EXPORT_CSV");
        log.setEntityName("Achievement");
        log.setPerformedBy(userEmail);

        String filterSummary = "keyword=" + searchDto.getKeyword() +
                ", branch=" + searchDto.getBranch() +
                ", year=" + searchDto.getYear() +
                ", categoryId=" + searchDto.getCategoryId() +
                ", level=" + searchDto.getLevel() +
                ", from=" + searchDto.getFromDate() +
                ", to=" + searchDto.getToDate() +
                ", rowCount=" + achievements.size();
        log.setNewValue(filterSummary);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);

        // Send CSV file download
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"achievements_export.csv\"");
        response.setContentLength(csvData.length);
        response.getOutputStream().write(csvData);
        response.getOutputStream().flush();
    }
}
