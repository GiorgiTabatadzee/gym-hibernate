package com.epam.gym.web.controller;

import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.metrics.GymMetrics;
import com.epam.gym.web.config.WebMvcConfig;
import com.epam.gym.web.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({WebMvcConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private GymMetrics metrics;

    @Test
    void login_returns200_onValidCredentials() throws Exception {
        mockMvc.perform(get("/api/auth/login")
                        .param("username", "giorgi.beridze")
                        .param("password", "Secret123!"))
                .andExpect(status().isOk());

        verify(authenticationService).authenticate("giorgi.beridze", "Secret123!");
        verify(metrics).incrementLoginSuccess();
    }

    @Test
    void login_returns401_onInvalidCredentials() throws Exception {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticate("giorgi.beridze", "wrong");

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "giorgi.beridze")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized());

        verify(metrics).incrementLoginFailure();
    }

    @Test
    void login_returns400_whenUsernameMissing() throws Exception {
        mockMvc.perform(get("/api/auth/login").param("password", "Secret123!"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_returns200() throws Exception {
        mockMvc.perform(put("/api/auth/password")
                        .contentType("application/json")
                        .content("{\"username\":\"giorgi.beridze\",\"oldPassword\":\"Secret123!\","
                                + "\"newPassword\":\"NewSecret456!\"}"))
                .andExpect(status().isOk());

        verify(authenticationService).changePassword("giorgi.beridze", "Secret123!", "NewSecret456!");
    }

    @Test
    void changePassword_returns400_whenNewPasswordBlank() throws Exception {
        mockMvc.perform(put("/api/auth/password")
                        .contentType("application/json")
                        .content("{\"username\":\"giorgi.beridze\",\"oldPassword\":\"Secret123!\","
                                + "\"newPassword\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_returns401_onWrongOldPassword() throws Exception {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).changePassword("giorgi.beridze", "wrong", "NewSecret456!");

        mockMvc.perform(put("/api/auth/password")
                        .contentType("application/json")
                        .content("{\"username\":\"giorgi.beridze\",\"oldPassword\":\"wrong\","
                                + "\"newPassword\":\"NewSecret456!\"}"))
                .andExpect(status().isUnauthorized());
    }
}
