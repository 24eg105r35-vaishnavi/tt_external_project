package com.portal.controller;

import com.portal.dto.AnalyticsSummaryDto;
import com.portal.dto.ChartItemDto;
import com.portal.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsRestController {

    private final AnalyticsService analyticsService;

    public AnalyticsRestController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // college-wide analytics dashboard metrics and charts
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnalytics(@RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        AnalyticsSummaryDto summary = analyticsService.getSummary(from, to);
        List<ChartItemDto> byCategory = analyticsService.getByCategory(from, to);
        List<ChartItemDto> byBranch = analyticsService.getByBranch(from, to);
        List<ChartItemDto> byLevel = analyticsService.getByLevel(from, to);
        List<ChartItemDto> byYear = analyticsService.getByYear(from, to);

        Map<String, Object> response = new HashMap<>();
        response.put("summary", summary);
        response.put("byCategory", byCategory);
        response.put("byBranch", byBranch);
        response.put("byLevel", byLevel);
        response.put("byYear", byYear);
        response.put("from", from);
        response.put("to", to);

        return ResponseEntity.ok(response);
    }
}
