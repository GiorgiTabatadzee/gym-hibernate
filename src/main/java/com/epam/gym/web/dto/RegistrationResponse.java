package com.epam.gym.web.dto;

import io.swagger.annotations.ApiModelProperty;

/** Response for both Trainee and Trainer registration: the generated login credentials. */
public class RegistrationResponse {

    @ApiModelProperty(value = "Generated login username", example = "john.doe")
    private String username;

    @ApiModelProperty(value = "Generated login password", example = "aB3xQ9zK1m")
    private String password;

    public RegistrationResponse() {
    }

    public RegistrationResponse(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
