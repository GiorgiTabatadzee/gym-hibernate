package com.epam.gym.dto;

import java.time.LocalDate;

/** Input payload for functionality #16: Add training. */
public class TrainingCreateRequest {

    private String traineeUsername;
    private String trainerUsername;
    private String trainingName;
    private LocalDate trainingDate;
    private Integer trainingDurationMinutes;

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

    public Integer getTrainingDurationMinutes() {
        return trainingDurationMinutes;
    }

    public void setTrainingDurationMinutes(Integer trainingDurationMinutes) {
        this.trainingDurationMinutes = trainingDurationMinutes;
    }
}
