package com.epam.gym.web.dto;

import com.epam.gym.entity.Trainer;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/** Response for #8 Get Trainer Profile and #9 Update Trainer Profile. */
public class TrainerProfileResponse {

    @ApiModelProperty(value = "Username", example = "nino.kapanadze")
    private String username;

    @ApiModelProperty(value = "First name", example = "Nino")
    private String firstName;

    @ApiModelProperty(value = "Last name", example = "Kapanadze")
    private String lastName;

    @ApiModelProperty(value = "Specialization (training type reference)", example = "Cardio")
    private String specialization;

    @ApiModelProperty(value = "Whether the account is active")
    private boolean active;

    @ApiModelProperty(value = "Assigned trainees")
    private List<TraineeShortResponse> trainees;

    public static TrainerProfileResponse from(Trainer trainer) {
        TrainerProfileResponse response = new TrainerProfileResponse();
        response.username = trainer.getUser().getUsername();
        response.firstName = trainer.getUser().getFirstName();
        response.lastName = trainer.getUser().getLastName();
        response.specialization = trainer.getSpecialization() != null
                ? trainer.getSpecialization().getTrainingTypeName() : null;
        response.active = Boolean.TRUE.equals(trainer.getUser().getIsActive());
        response.trainees = trainer.getTrainees().stream().map(TraineeShortResponse::from).toList();
        return response;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<TraineeShortResponse> getTrainees() {
        return trainees;
    }

    public void setTrainees(List<TraineeShortResponse> trainees) {
        this.trainees = trainees;
    }
}
