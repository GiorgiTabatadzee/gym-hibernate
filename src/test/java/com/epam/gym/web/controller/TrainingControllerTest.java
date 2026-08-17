package com.epam.gym.web.controller;

import com.epam.gym.dto.TrainingCreateRequest;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.service.TrainingService;
import com.epam.gym.metrics.GymMetrics;
import com.epam.gym.web.config.WebMvcConfig;
import com.epam.gym.web.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.epam.gym.web.TestSupport.basicAuthHeader;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@Import({WebMvcConfig.class, GlobalExceptionHandler.class})
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private GymMetrics metrics;

    private static final String VALID_BODY = "{\"traineeUsername\":\"giorgi.beridze\","
            + "\"trainerUsername\":\"nino.kapanadze\",\"trainingName\":\"Morning Cardio\","
            + "\"trainingDate\":\"2026-08-10\",\"trainingDuration\":45}";

    @Test
    void addTraining_returns200() throws Exception {
        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("nino.kapanadze", "Secret123!"))
                        .contentType("application/json")
                        .content(VALID_BODY))
                .andExpect(status().isOk());

        verify(trainingService).addTraining(any(TrainingCreateRequest.class), org.mockito.ArgumentMatchers.eq("Secret123!"));
        verify(metrics).incrementTrainingsAdded();
    }

    @Test
    void addTraining_returns401_withoutAuth() throws Exception {
        mockMvc.perform(post("/api/trainings")
                        .contentType("application/json")
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addTraining_returns400_whenDurationNotPositive() throws Exception {
        String body = "{\"traineeUsername\":\"giorgi.beridze\",\"trainerUsername\":\"nino.kapanadze\","
                + "\"trainingName\":\"Morning Cardio\",\"trainingDate\":\"2026-08-10\",\"trainingDuration\":0}";

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("nino.kapanadze", "Secret123!"))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_returns401_onWrongTrainerPassword() throws Exception {
        when(trainingService.addTraining(any(TrainingCreateRequest.class), anyString()))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("nino.kapanadze", "wrong"))
                        .contentType("application/json")
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addTraining_returns404_whenTraineeUnknown() throws Exception {
        when(trainingService.addTraining(any(TrainingCreateRequest.class), anyString()))
                .thenThrow(new EntityNotFoundException("Trainee not found: giorgi.beridze"));

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("nino.kapanadze", "Secret123!"))
                        .contentType("application/json")
                        .content(VALID_BODY))
                .andExpect(status().isNotFound());
    }
}
