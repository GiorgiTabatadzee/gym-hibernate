package com.epam.gym.web.dto;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request payload for #11 Update Trainee's Trainer List. The trainee username is taken from the
 * path (the resource identifier); the body carries the full replacement trainer list.
 */
public class UpdateTraineeTrainersRequest {

    @NotEmpty(message = "trainers list is required")
    @ApiModelProperty(value = "Full replacement list of trainer usernames", required = true)
    private List<@NotBlank(message = "trainer username must not be blank") String> trainers;

    public List<String> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<String> trainers) {
        this.trainers = trainers;
    }
}
