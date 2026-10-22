package com.portal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnalyticsSummaryDto {
    private long totalApproved;
    private long totalStudents;
    private long totalPoints;

    public AnalyticsSummaryDto(long totalApproved, long totalStudents, long totalPoints) {
        this.totalApproved = totalApproved;
        this.totalStudents = totalStudents;
        this.totalPoints = totalPoints;
    }

    public long getTotalApproved() {
        return totalApproved;
    }

    public void setTotalApproved(long totalApproved) {
        this.totalApproved = totalApproved;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(long totalPoints) {
        this.totalPoints = totalPoints;
    }
}
