package com.portal.controller;

import com.portal.dto.LeaderboardRowDto;
import com.portal.repository.AchievementRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class LeaderboardController {

    private final AchievementRepository achievementRepository;

    public LeaderboardController(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @GetMapping("/leaderboard")
    public String showLeaderboard(@RequestParam(value = "branch", required = false) String branch,
                                  @RequestParam(value = "year", required = false) Integer year,
                                  Model model) {

        // normalize empty branch string to null
        String normalizedBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : null;

        // query top 10 students by approved points, skipping PRIVATE profiles
        List<Object[]> rawRows = achievementRepository.findTopStudentsLeaderboard(
                normalizedBranch,
                year,
                PageRequest.of(0, 10)
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

        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("selectedBranch", normalizedBranch);
        model.addAttribute("selectedYear", year);
        model.addAttribute("branches", Arrays.asList("CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "AI&DS"));
        model.addAttribute("years", Arrays.asList(1, 2, 3, 4));

        return "leaderboard/list";
    }
}
