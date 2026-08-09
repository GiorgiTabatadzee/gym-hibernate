package com.epam.gym.web.dto;

import com.epam.gym.entity.Trainee;
import io.swagger.annotations.ApiModelProperty;

/** Trainee summary, embedded in trainer-profile responses. */
public class TraineeShortResponse {

    @ApiModelProperty(value = "Trainee username", example = "giorgi.beridze")
    private String username;

    @ApiModelProperty(value = "Trainee first name", example = "Giorgi")
    private String firstName;

    @ApiModelProperty(value = "Trainee last name", example = "Beridze")
    private String lastName;

    public TraineeShortResponse() {
    }

    public TraineeShortResponse(String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static TraineeShortResponse from(Trainee trainee) {
        return new TraineeShortResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName());
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
}
