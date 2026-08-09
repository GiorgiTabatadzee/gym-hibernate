package com.epam.gym.web.dto;

import com.epam.gym.entity.Training;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;

/** Response item for #12 Get Trainee Trainings List. */
public class TraineeTrainingResponse {

    @ApiModelProperty(value = "Training name")
    private String trainingName;

    @ApiModelProperty(value = "Training date")
    private LocalDate trainingDate;

    @ApiModelProperty(value = "Training type (reference)")
    private String trainingType;

    @ApiModelProperty(value = "Training duration, in minutes")
    private Integer trainingDuration;

    @ApiModelProperty(value = "Trainer full name")
    private String trainerName;

    public static TraineeTrainingResponse from(Training training) {
        TraineeTrainingResponse response = new TraineeTrainingResponse();
        response.trainingName = training.getTrainingName();
        response.trainingDate = training.getTrainingDate();
        response.trainingType = training.getTrainingType().getTrainingTypeName();
        response.trainingDuration = training.getTrainingDuration();
        response.trainerName = training.getTrainer().getUser().getFirstName()
                + " " + training.getTrainer().getUser().getLastName();
        return response;
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

    public String getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(String trainingType) {
        this.trainingType = trainingType;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }
}
