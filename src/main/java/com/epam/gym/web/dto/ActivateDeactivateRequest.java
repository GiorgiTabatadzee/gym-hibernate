package com.epam.gym.web.dto;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for #15/#16 Activate/De-Activate Trainee/Trainer. Username is taken from the
 * path (the resource identifier), so the body only carries the target state.
 */
public class ActivateDeactivateRequest {

    @NotNull(message = "isActive is required")
    @ApiModelProperty(value = "Target active state", required = true, example = "true")
    private Boolean isActive;

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
