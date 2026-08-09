package com.epam.gym.web.dto;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/** Request payload for #14 Add Training. */
public class AddTrainingRequest {

    @NotBlank(message = "traineeUsername is required")
    @ApiModelProperty(value = "Trainee username", required = true, example = "giorgi.beridze")
    private String traineeUsername;

    @NotBlank(message = "trainerUsername is required")
    @ApiModelProperty(value = "Trainer username", required = true, example = "nino.kapanadze")
    private String trainerUsername;

    @NotBlank(message = "trainingName is required")
    @ApiModelProperty(value = "Training name", required = true, example = "Morning Cardio Session")
    private String trainingName;

    @NotNull(message = "trainingDate is required")
    @ApiModelProperty(value = "Training date", required = true)
    private LocalDate trainingDate;

    @NotNull(message = "trainingDuration is required")
    @Positive(message = "trainingDuration must be a positive number")
    @ApiModelProperty(value = "Training duration, in minutes", required = true, example = "45")
    private Integer trainingDuration;

    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }
}
