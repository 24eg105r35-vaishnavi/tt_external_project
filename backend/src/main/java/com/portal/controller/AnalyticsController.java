package com.portal.controller;

import com.portal.dto.AnalyticsSummaryDto;
import com.portal.dto.ChartItemDto;
import com.portal.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics")
    public String showAnalytics(@RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                Model model) {

        AnalyticsSummaryDto summary = analyticsService.getSummary(from, to);
        List<ChartItemDto> byCategory = analyticsService.getByCategory(from, to);
        List<ChartItemDto> byBranch = analyticsService.getByBranch(from, to);
        List<ChartItemDto> byLevel = analyticsService.getByLevel(from, to);
        List<ChartItemDto> byYear = analyticsService.getByYear(from, to);

        model.addAttribute("summary", summary);
        model.addAttribute("byCategory", byCategory);
        model.addAttribute("byBranch", byBranch);
        model.addAttribute("byLevel", byLevel);
        model.addAttribute("byYear", byYear);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "analytics/dashboard";
    }
}
