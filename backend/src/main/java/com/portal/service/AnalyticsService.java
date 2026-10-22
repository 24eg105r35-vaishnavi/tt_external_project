package com.portal.service;

import com.portal.dto.AnalyticsSummaryDto;
import com.portal.dto.ChartItemDto;
import com.portal.repository.AchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    private final AchievementRepository achievementRepository;

    public AnalyticsService(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    // get summary cards numbers
    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getSummary(LocalDate fromDate, LocalDate toDate) {
        long totalApproved = achievementRepository.countTotalApproved(fromDate, toDate);
        long totalStudents = achievementRepository.countStudentsWithApproved(fromDate, toDate);
        long totalPoints = achievementRepository.sumTotalPointsAwarded(fromDate, toDate);

        return new AnalyticsSummaryDto(totalApproved, totalStudents, totalPoints);
    }

    // get achievement counts by category
    @Transactional(readOnly = true)
    public List<ChartItemDto> getByCategory(LocalDate fromDate, LocalDate toDate) {
        List<Object[]> rows = achievementRepository.countApprovedByCategory(fromDate, toDate);
        return convertToChartItems(rows);
    }

    // get achievement counts by branch
    @Transactional(readOnly = true)
    public List<ChartItemDto> getByBranch(LocalDate fromDate, LocalDate toDate) {
        List<Object[]> rows = achievementRepository.countApprovedByBranch(fromDate, toDate);
        return convertToChartItems(rows);
    }

    // get achievement counts by level
    @Transactional(readOnly = true)
    public List<ChartItemDto> getByLevel(LocalDate fromDate, LocalDate toDate) {
        List<Object[]> rows = achievementRepository.countApprovedByLevel(fromDate, toDate);
        return convertToChartItems(rows);
    }

    // get achievement counts by event year
    @Transactional(readOnly = true)
    public List<ChartItemDto> getByYear(LocalDate fromDate, LocalDate toDate) {
        List<Object[]> rows = achievementRepository.countApprovedByEventYear(fromDate, toDate);
        return convertToChartItems(rows);
    }

    // helper method to calculate bar percentage for each row
    private List<ChartItemDto> convertToChartItems(List<Object[]> rows) {
        List<ChartItemDto> items = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return items;
        }

        // find max count first to scale bars to 100%
        long maxCount = 0;
        for (Object[] row : rows) {
            long count = ((Number) row[1]).longValue();
            if (count > maxCount) {
                maxCount = count;
            }
        }

        for (Object[] row : rows) {
            String label = row[0] != null ? String.valueOf(row[0]) : "Unknown";
            long count = ((Number) row[1]).longValue();
            int percentage = 0;
            if (maxCount > 0) {
                percentage = (int) Math.round(((double) count / maxCount) * 100);
            }
            items.add(new ChartItemDto(label, count, percentage));
        }

        return items;
    }
}
