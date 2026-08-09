package com.epam.gym.web.dto;

import com.epam.gym.entity.TrainingType;
import io.swagger.annotations.ApiModelProperty;

/** Response item for #17 Get Training Types. */
public class TrainingTypeResponse {

    @ApiModelProperty(value = "Training type name", example = "Cardio")
    private String trainingType;

    @ApiModelProperty(value = "Training type id", example = "1")
    private Long trainingTypeId;

    public TrainingTypeResponse() {
    }

    public TrainingTypeResponse(String trainingType, Long trainingTypeId) {
        this.trainingType = trainingType;
        this.trainingTypeId = trainingTypeId;
    }

    public static TrainingTypeResponse from(TrainingType trainingType) {
        return new TrainingTypeResponse(trainingType.getTrainingTypeName(), trainingType.getId());
    }

    public String getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(String trainingType) {
        this.trainingType = trainingType;
    }

    public Long getTrainingTypeId() {
        return trainingTypeId;
    }

    public void setTrainingTypeId(Long trainingTypeId) {
        this.trainingTypeId = trainingTypeId;
    }
}
