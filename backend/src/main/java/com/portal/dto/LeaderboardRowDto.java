package com.portal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaderboardRowDto {
    private int rank;
    private String fullName;
    private String branch;
    private int year;
    private long totalPoints;
    private long achievementCount;

    public LeaderboardRowDto(int rank, String fullName, String branch, int year, long totalPoints, long achievementCount) {
        this.rank = rank;
        this.fullName = fullName;
        this.branch = branch;
        this.year = year;
        this.totalPoints = totalPoints;
        this.achievementCount = achievementCount;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public long getAchievementCount() {
        return achievementCount;
    }

    public void setAchievementCount(long achievementCount) {
        this.achievementCount = achievementCount;
    }
}
