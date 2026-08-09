package com.epam.gym.web.dto;

import com.epam.gym.entity.Training;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;

/** Response item for #13 Get Trainer Trainings List. */
public class TrainerTrainingResponse {

    @ApiModelProperty(value = "Training name")
    private String trainingName;

    @ApiModelProperty(value = "Training date")
    private LocalDate trainingDate;

    @ApiModelProperty(value = "Training type (reference)")
    private String trainingType;

    @ApiModelProperty(value = "Training duration, in minutes")
    private Integer trainingDuration;

    @ApiModelProperty(value = "Trainee full name")
    private String traineeName;

    public static TrainerTrainingResponse from(Training training) {
        TrainerTrainingResponse response = new TrainerTrainingResponse();
        response.trainingName = training.getTrainingName();
        response.trainingDate = training.getTrainingDate();
        response.trainingType = training.getTrainingType().getTrainingTypeName();
        response.trainingDuration = training.getTrainingDuration();
        response.traineeName = training.getTrainee().getUser().getFirstName()
                + " " + training.getTrainee().getUser().getLastName();
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

    public String getTraineeName() {
        return traineeName;
    }

    public void setTraineeName(String traineeName) {
        this.traineeName = traineeName;
    }
}
