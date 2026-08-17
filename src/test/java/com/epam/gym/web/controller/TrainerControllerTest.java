package com.epam.gym.web.controller;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.IllegalStateTransitionException;
import com.epam.gym.service.TrainerService;
import com.epam.gym.metrics.GymMetrics;
import com.epam.gym.web.config.WebMvcConfig;
import com.epam.gym.web.error.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static com.epam.gym.web.TestSupport.basicAuthHeader;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerController.class)
@Import({WebMvcConfig.class, GlobalExceptionHandler.class})
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainerService trainerService;

    @MockitoBean
    private GymMetrics metrics;

    private Trainer trainer;
    private Trainee trainee;

    @BeforeEach
    void setUp() {
        TrainingType cardio = new TrainingType("Cardio");
        cardio.setId(1L);
        User trainerUser = new User("Nino", "Kapanadze", "nino.kapanadze", "Secret123!", true);
        trainer = new Trainer(cardio, trainerUser);
        trainer.setId(1L);

        User traineeUser = new User("Giorgi", "Beridze", "giorgi.beridze", "TraineePass1!", true);
        trainee = new Trainee(LocalDate.of(2000, 5, 20), "Tbilisi", traineeUser);
        trainee.setId(2L);
    }

    @Test
    void register_returns201WithCredentials() throws Exception {
        when(trainerService.createTrainerProfile("Nino", "Kapanadze", "Cardio")).thenReturn(trainer);

        mockMvc.perform(post("/api/trainers")
                        .contentType("application/json")
                        .content("{\"firstName\":\"Nino\",\"lastName\":\"Kapanadze\",\"specialization\":\"Cardio\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("nino.kapanadze"));

        verify(metrics).incrementTrainerRegistrations();
    }

    @Test
    void register_returns400_whenSpecializationBlank() throws Exception {
        mockMvc.perform(post("/api/trainers")
                        .contentType("application/json")
                        .content("{\"firstName\":\"Nino\",\"lastName\":\"Kapanadze\",\"specialization\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns404_whenSpecializationUnknown() throws Exception {
        when(trainerService.createTrainerProfile(anyString(), anyString(), eq("Underwater Basket Weaving")))
                .thenThrow(new EntityNotFoundException("Unknown specialization: Underwater Basket Weaving"));

        mockMvc.perform(post("/api/trainers")
                        .contentType("application/json")
                        .content("{\"firstName\":\"Nino\",\"lastName\":\"Kapanadze\","
                                + "\"specialization\":\"Underwater Basket Weaving\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfile_returns200() throws Exception {
        when(trainerService.getProfileByUsername("nino.kapanadze", "Secret123!")).thenReturn(trainer);

        mockMvc.perform(get("/api/trainers/nino.kapanadze")
                        .header("Authorization", basicAuthHeader("nino.kapanadze", "Secret123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialization").value("Cardio"));
    }

    @Test
    void getProfile_returns401_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/trainers/nino.kapanadze"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_returns200() throws Exception {
        when(trainerService.updateProfile(eq("nino.kapanadze"), eq("Secret123!"), eq("Nino"), eq("Kapanadze"),
                eq(false))).thenReturn(trainer);

        mockMvc.perform(put("/api/trainers/nino.kapanadze")
                        .header("Authorization", basicAuthHeader("nino.kapanadze", "Secret123!"))
                        .contentType("application/json")
                        .content("{\"firstName\":\"Nino\",\"lastName\":\"Kapanadze\",\"isActive\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    void getTrainings_returns200() throws Exception {
        Training training = new Training(trainee, trainer, "Morning Cardio", trainer.getSpecialization(),
                LocalDate.now(), 45);
        when(trainerService.getTrainingsList(eq("nino.kapanadze"), eq("Secret123!"), any()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainers/nino.kapanadze/trainings")
                        .header("Authorization", basicAuthHeader("nino.kapanadze", "Secret123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].traineeName").value("Giorgi Beridze"));
    }

    @Test
    void setActive_returns200() throws Exception {
        mockMvc.perform(patch("/api/trainers/nino.kapanadze/status")
                        .header("Authorization", basicAuthHeader("nino.kapanadze", "Secret123!"))
                        .contentType("application/json")
                        .content("{\"isActive\":false}"))
                .andExpect(status().isOk());

        verify(trainerService).setActive("nino.kapanadze", "Secret123!", false);
    }

    @Test
    void setActive_returns409_whenAlreadyInRequestedState() throws Exception {
        doThrow(new IllegalStateTransitionException("Trainer 'nino.kapanadze' is already inactive"))
                .when(trainerService).setActive(anyString(), anyString(), anyBoolean());

        mockMvc.perform(patch("/api/trainers/nino.kapanadze/status")
                        .header("Authorization", basicAuthHeader("nino.kapanadze", "Secret123!"))
                        .contentType("application/json")
                        .content("{\"isActive\":false}"))
                .andExpect(status().isConflict());
    }
}
