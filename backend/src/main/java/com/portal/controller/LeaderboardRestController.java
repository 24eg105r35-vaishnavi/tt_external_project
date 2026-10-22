package com.portal.controller;

import com.portal.dto.LeaderboardRowDto;
import com.portal.repository.AchievementRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardRestController {

    private final AchievementRepository achievementRepository;

    private final List<String> branches = Arrays.asList("CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "AI&DS");
    private final List<Integer> years = Arrays.asList(1, 2, 3, 4);

    public LeaderboardRestController(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    // public leaderboard of top verified student achievers (excluding PRIVATE profiles)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getLeaderboard(@RequestParam(value = "branch", required = false) String branch,
                                                              @RequestParam(value = "year", required = false) Integer year) {
        String normalizedBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : null;

        List<Object[]> rawRows = achievementRepository.findTopStudentsLeaderboard(
                normalizedBranch,
                year,
                PageRequest.of(0, 20)
        );

        List<LeaderboardRowDto> leaderboard = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rawRows) {
            String fullName = (String) row[0];
            String studentBranch = (String) row[1];
            int studentYear = ((Number) row[2]).intValue();
            long totalPoints = ((Number) row[3]).longValue();
            long achievementCount = ((Number) row[4]).longValue();

            leaderboard.add(new LeaderboardRowDto(rank++, fullName, studentBranch, studentYear, totalPoints, achievementCount));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("leaderboard", leaderboard);
        response.put("branches", branches);
        response.put("years", years);
        response.put("selectedBranch", normalizedBranch);
        response.put("selectedYear", year);

        return ResponseEntity.ok(response);
    }
}
