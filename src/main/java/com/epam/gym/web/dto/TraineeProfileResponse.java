package com.epam.gym.web.dto;

import com.epam.gym.entity.Trainee;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.List;

/** Response for #5 Get Trainee Profile and #6 Update Trainee Profile. */
public class TraineeProfileResponse {

    @ApiModelProperty(value = "Username", example = "giorgi.beridze")
    private String username;

    @ApiModelProperty(value = "First name", example = "Giorgi")
    private String firstName;

    @ApiModelProperty(value = "Last name", example = "Beridze")
    private String lastName;

    @ApiModelProperty(value = "Date of birth")
    private LocalDate dateOfBirth;

    @ApiModelProperty(value = "Address")
    private String address;

    @ApiModelProperty(value = "Whether the account is active")
    private boolean active;

    @ApiModelProperty(value = "Assigned trainers")
    private List<TrainerShortResponse> trainers;

    public static TraineeProfileResponse from(Trainee trainee) {
        TraineeProfileResponse response = new TraineeProfileResponse();
        response.username = trainee.getUser().getUsername();
        response.firstName = trainee.getUser().getFirstName();
        response.lastName = trainee.getUser().getLastName();
        response.dateOfBirth = trainee.getDateOfBirth();
        response.address = trainee.getAddress();
        response.active = Boolean.TRUE.equals(trainee.getUser().getIsActive());
        response.trainers = trainee.getTrainers().stream().map(TrainerShortResponse::from).toList();
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<TrainerShortResponse> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<TrainerShortResponse> trainers) {
        this.trainers = trainers;
    }
}
