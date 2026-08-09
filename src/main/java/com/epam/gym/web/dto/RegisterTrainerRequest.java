package com.epam.gym.web.dto;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;

/** Request payload for #2 Trainer Registration. */
public class RegisterTrainerRequest {

    @NotBlank(message = "firstName is required")
    @ApiModelProperty(value = "Trainer first name", required = true, example = "Nino")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @ApiModelProperty(value = "Trainer last name", required = true, example = "Kapanadze")
    private String lastName;

    @NotBlank(message = "specialization is required")
    @ApiModelProperty(value = "Specialization — training type name (reference)", required = true, example = "Cardio")
    private String specialization;

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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
