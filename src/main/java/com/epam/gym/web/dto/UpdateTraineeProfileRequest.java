package com.epam.gym.web.dto;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request payload for #6 Update Trainee Profile. Username is taken from the path (the resource
 * identifier — it cannot be changed), so the body only carries the mutable fields.
 */
public class UpdateTraineeProfileRequest {

    @NotBlank(message = "firstName is required")
    @ApiModelProperty(value = "First name", required = true, example = "Giorgi")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @ApiModelProperty(value = "Last name", required = true, example = "Beridze")
    private String lastName;

    @ApiModelProperty(value = "Date of birth (optional)")
    private LocalDate dateOfBirth;

    @ApiModelProperty(value = "Address (optional)")
    private String address;

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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
