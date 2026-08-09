package com.epam.gym.web.dto;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;

/** Request payload for #4 Change Login. */
public class ChangeLoginRequest {

    @NotBlank(message = "username is required")
    @ApiModelProperty(value = "Username", required = true, example = "john.doe")
    private String username;

    @NotBlank(message = "oldPassword is required")
    @ApiModelProperty(value = "Current password", required = true)
    private String oldPassword;

    @NotBlank(message = "newPassword is required")
    @ApiModelProperty(value = "New password", required = true)
    private String newPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
