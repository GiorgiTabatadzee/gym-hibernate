package com.epam.gym.web.controller;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.IllegalStateTransitionException;
import com.epam.gym.service.TraineeService;
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
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeController.class)
@Import({WebMvcConfig.class, GlobalExceptionHandler.class})
class TraineeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TraineeService traineeService;

    @MockitoBean
    private GymMetrics metrics;

    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        User traineeUser = new User("Giorgi", "Beridze", "giorgi.beridze", "Secret123!", true);
        trainee = new Trainee(LocalDate.of(2000, 5, 20), "Tbilisi", traineeUser);
        trainee.setId(1L);

        TrainingType cardio = new TrainingType("Cardio");
        cardio.setId(1L);
        User trainerUser = new User("Nino", "Kapanadze", "nino.kapanadze", "TrainerPass1!", true);
        trainer = new Trainer(cardio, trainerUser);
        trainer.setId(2L);
    }

    @Test
    void register_returns201WithCredentials() throws Exception {
        when(traineeService.createTraineeProfile(eq("Giorgi"), eq("Beridze"), any(), any())).thenReturn(trainee);

        mockMvc.perform(post("/api/trainees")
                        .contentType("application/json")
                        .content("{\"firstName\":\"Giorgi\",\"lastName\":\"Beridze\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("giorgi.beridze"))
                .andExpect(jsonPath("$.password").value("Secret123!"));

        verify(metrics).incrementTraineeRegistrations();
    }

    @Test
    void register_returns400WhenFirstNameBlank() throws Exception {
        mockMvc.perform(post("/api/trainees")
                        .contentType("application/json")
                        .content("{\"firstName\":\"\",\"lastName\":\"Beridze\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getProfile_returns200_withValidAuth() throws Exception {
        when(traineeService.getProfileByUsername("giorgi.beridze", "Secret123!")).thenReturn(trainee);

        mockMvc.perform(get("/api/trainees/giorgi.beridze")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "Secret123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("giorgi.beridze"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getProfile_returns401_whenAuthorizationHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/trainees/giorgi.beridze"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_returns401_whenCredentialsWrong() throws Exception {
        when(traineeService.getProfileByUsername("giorgi.beridze", "wrong"))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        mockMvc.perform(get("/api/trainees/giorgi.beridze")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_returns404_whenTraineeUnknown() throws Exception {
        when(traineeService.getProfileByUsername("ghost", "x"))
                .thenThrow(new EntityNotFoundException("Trainee not found: ghost"));

        mockMvc.perform(get("/api/trainees/ghost")
                        .header("Authorization", basicAuthHeader("ghost", "x")))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfile_returns200() throws Exception {
        when(traineeService.updateProfile(eq("giorgi.beridze"), eq("Secret123!"), eq("Giorgi"), eq("Beridze"),
                any(), any(), eq(true))).thenReturn(trainee);

        mockMvc.perform(put("/api/trainees/giorgi.beridze")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "Secret123!"))
                        .contentType("application/json")
                        .content("{\"firstName\":\"Giorgi\",\"lastName\":\"Beridze\",\"isActive\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("giorgi.beridze"));
    }

    @Test
    void updateProfile_returns400_whenIsActiveMissing() throws Exception {
        mockMvc.perform(put("/api/trainees/giorgi.beridze")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "Secret123!"))
                        .contentType("application/json")
                        .content("{\"firstName\":\"Giorgi\",\"lastName\":\"Beridze\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteProfile_returns200() throws Exception {
        mockMvc.perform(delete("/api/trainees/giorgi.beridze")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "Secret123!")))
                .andExpect(status().isOk());

        verify(traineeService).deleteProfileByUsername("giorgi.beridze", "Secret123!");
    }

    @Test
    void getUnassignedTrainers_returns200() throws Exception {
        when(traineeService.getTrainersNotAssigned("giorgi.beridze", "Secret123!")).thenReturn(List.of(trainer));

        mockMvc.perform(get("/api/trainees/giorgi.beridze/unassigned-trainers")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "Secret123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("nino.kapanadze"));
    }

    @Test
    void updateTrainers_returns200() throws Exception {
        when(traineeService.updateTrainersList(eq("giorgi.beridze"), eq("Secret123!"), anyList()))
                .thenReturn(List.of(trainer));

        mockMvc.perform(put("/api/trainees/giorgi.beridze/trainers")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "Secret123!"))
                        .contentType("application/json")
                        .content("{\"trainers\":[\"nino.kapanadze\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialization").value("Cardio"));
    }

    @Test
    void updateTrainers_returns400_whenListEmpty() throws Exception {
        mockMvc.perform(put("/api/trainees/giorgi.beridze/trainers")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "Secret123!"))
                        .contentType("application/json")
                        .content("{\"trainers\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrainings_returns200() throws Exception {
        Training training = new Training(trainee, trainer, "Morning Cardio", trainer.getSpecialization(),
                LocalDate.now(), 45);
        when(traineeService.getTrainingsList(eq("giorgi.beridze"), eq("Secret123!"), any()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainees/giorgi.beridze/trainings")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "Secret123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Cardio"))
                .andExpect(jsonPath("$[0].trainerName").value("Nino Kapanadze"));
    }

    @Test
    void setActive_returns200() throws Exception {
        mockMvc.perform(patch("/api/trainees/giorgi.beridze/status")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "Secret123!"))
                        .contentType("application/json")
                        .content("{\"isActive\":false}"))
                .andExpect(status().isOk());

        verify(traineeService).setActive("giorgi.beridze", "Secret123!", false);
    }

    @Test
    void setActive_returns409_whenAlreadyInRequestedState() throws Exception {
        doThrow(new IllegalStateTransitionException("Trainee 'giorgi.beridze' is already active"))
                .when(traineeService).setActive(anyString(), anyString(), anyBoolean());

        mockMvc.perform(patch("/api/trainees/giorgi.beridze/status")
                        .header("Authorization", basicAuthHeader("giorgi.beridze", "Secret123!"))
                        .contentType("application/json")
                        .content("{\"isActive\":true}"))
                .andExpect(status().isConflict());
    }
}
