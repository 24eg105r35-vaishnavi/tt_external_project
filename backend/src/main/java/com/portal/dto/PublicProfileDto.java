package com.portal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class PublicProfileDto {
    private String fullName;
    private String branch;
    private int year;
    private int totalPoints;
    private com.portal.entity.Visibility visibility;
    private Map<String, Integer> categoryCounts;
    private Map<String, List<PublicAchievementDto>> groupedAchievements;

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

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Map<String, Integer> getCategoryCounts() {
        return categoryCounts;
    }

    public void setCategoryCounts(Map<String, Integer> categoryCounts) {
        this.categoryCounts = categoryCounts;
    }

    public com.portal.entity.Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(com.portal.entity.Visibility visibility) {
        this.visibility = visibility;
    }

    public Map<String, List<PublicAchievementDto>> getGroupedAchievements() {
        return groupedAchievements;
    }

    public void setGroupedAchievements(Map<String, List<PublicAchievementDto>> groupedAchievements) {
        this.groupedAchievements = groupedAchievements;
    }
}
