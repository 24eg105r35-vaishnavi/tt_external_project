package com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DecisionDto {

    // Decision action: APPROVE, REJECT, or SEND_BACK
    @NotBlank(message = "Decision is required.")
    private String decision;

    // Feedback or reason from faculty
    private String reason;

    // default constructor for spring form binding
    public DecisionDto() {
    }

    public DecisionDto(String decision, String reason) {
        this.decision = decision;
        this.reason = reason;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
