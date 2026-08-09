package com.epam.gym.web.dto;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for #9 Update Trainer Profile. Username is taken from the path (the resource
 * identifier). Specialization is read-only and not accepted here.
 */
public class UpdateTrainerProfileRequest {

    @NotBlank(message = "firstName is required")
    @ApiModelProperty(value = "First name", required = true, example = "Nino")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @ApiModelProperty(value = "Last name", required = true, example = "Kapanadze")
    private String lastName;

    @NotNull(message = "isActive is required")
    @ApiModelProperty(value = "Whether the account is active", required = true)
    private Boolean isActive;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
