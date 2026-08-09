package com.epam.gym.web.dto;

import com.epam.gym.entity.Trainer;
import io.swagger.annotations.ApiModelProperty;

/** Trainer summary, embedded in trainee-profile and trainer-list responses. */
public class TrainerShortResponse {

    @ApiModelProperty(value = "Trainer username", example = "nino.kapanadze")
    private String username;

    @ApiModelProperty(value = "Trainer first name", example = "Nino")
    private String firstName;

    @ApiModelProperty(value = "Trainer last name", example = "Kapanadze")
    private String lastName;

    @ApiModelProperty(value = "Trainer specialization (training type reference)", example = "Cardio")
    private String specialization;

    public TrainerShortResponse() {
    }

    public TrainerShortResponse(String username, String firstName, String lastName, String specialization) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
    }

    public static TrainerShortResponse from(Trainer trainer) {
        return new TrainerShortResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization() != null ? trainer.getSpecialization().getTrainingTypeName() : null);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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
