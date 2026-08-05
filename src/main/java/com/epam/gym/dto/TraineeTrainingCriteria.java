package com.epam.gym.dto;

import java.time.LocalDate;

/** Optional filter criteria for functionality #14: Get Trainee Trainings List. Any field may be null. */
public class TraineeTrainingCriteria {

    private LocalDate fromDate;
    private LocalDate toDate;
    private String trainerName;
    private String trainingTypeName;

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public String getTrainingTypeName() {
        return trainingTypeName;
    }

    public void setTrainingTypeName(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }
}
