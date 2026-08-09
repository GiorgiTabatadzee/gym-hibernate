package com.epam.gym.web.dto;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/** Request payload for #1 Trainee Registration. */
public class RegisterTraineeRequest {

    @NotBlank(message = "firstName is required")
    @ApiModelProperty(value = "Trainee first name", required = true, example = "Giorgi")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @ApiModelProperty(value = "Trainee last name", required = true, example = "Beridze")
    private String lastName;

    @ApiModelProperty(value = "Date of birth (optional)", example = "2000-05-20")
    private LocalDate dateOfBirth;

    @ApiModelProperty(value = "Address (optional)", example = "12 Rustaveli Ave, Tbilisi")
    private String address;

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
}
