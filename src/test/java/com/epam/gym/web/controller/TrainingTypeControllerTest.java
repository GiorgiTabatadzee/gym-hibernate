package com.epam.gym.web.controller;

import com.epam.gym.entity.TrainingType;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.service.TrainingTypeService;
import com.epam.gym.web.config.WebMvcConfig;
import com.epam.gym.web.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.epam.gym.web.TestSupport.basicAuthHeader;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingTypeController.class)
@Import({WebMvcConfig.class, GlobalExceptionHandler.class})
class TrainingTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingTypeService trainingTypeService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    void getTrainingTypes_returns200() throws Exception {
        TrainingType cardio = new TrainingType("Cardio");
        cardio.setId(1L);
        doNothing().when(authenticationService).authenticate("giorgi.beridze", "Secret123!");
        when(trainingTypeService.getAll()).thenReturn(List.of(cardio));

        mockMvc.perform(get("/api/training-types")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "Secret123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingType").value("Cardio"))
                .andExpect(jsonPath("$[0].trainingTypeId").value(1));
    }

    @Test
    void getTrainingTypes_returns401_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTrainingTypes_returns401_onInvalidCredentials() throws Exception {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authenticationService).authenticate("giorgi.beridze", "wrong");

        mockMvc.perform(get("/api/training-types")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "wrong")))
                .andExpect(status().isUnauthorized());
    }
}
