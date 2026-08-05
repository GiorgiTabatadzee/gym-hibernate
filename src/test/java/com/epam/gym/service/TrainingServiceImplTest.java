package com.epam.gym.service;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.dto.TrainingCreateRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.util.FakeTransactionExecutor;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private TrainingTypeDao trainingTypeDao;
    @Mock
    private TrainingDao trainingDao;

    private TrainingServiceImpl service;
    private Trainer trainer;
    private Trainee trainee;
    private TrainingType cardio;

    @BeforeEach
    void setUp() {
        service = new TrainingServiceImpl(traineeDao, trainerDao, trainingTypeDao, trainingDao,
                new FakeTransactionExecutor());
        User trainerUser = new User("Nino", "Kapanadze", "nino.kapanadze", "TrainerPass1!", true);
        trainer = new Trainer(null, trainerUser);
        trainer.setId(1L);
        User traineeUser = new User("Giorgi", "Beridze", "giorgi.beridze", "TraineePass1!", true);
        trainee = new Trainee(LocalDate.of(2000, 1, 1), "Tbilisi", traineeUser);
        trainee.setId(1L);
        cardio = new TrainingType("Cardio");
        cardio.setId(1L);
    }

    private TrainingCreateRequest validRequest() {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTraineeUsername("giorgi.beridze");
        request.setTrainerUsername("nino.kapanadze");
        request.setTrainingName("Morning Cardio");
        request.setTrainingTypeName("Cardio");
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDurationMinutes(45);
        return request;
    }

    @Test
    void addTraining_succeeds_withValidRequestAndCorrectPassword() {
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        when(trainingTypeDao.findByName(any(Session.class), eq("Cardio"))).thenReturn(Optional.of(cardio));
        when(trainingDao.save(any(Session.class), any(Training.class))).thenAnswer(inv -> inv.getArgument(1));

        Training result = service.addTraining(validRequest(), "TrainerPass1!");

        assertEquals("Morning Cardio", result.getTrainingName());
        assertEquals(45, result.getTrainingDuration().intValue());
    }

    @Test
    void addTraining_throwsAuthenticationException_onWrongTrainerPassword() {
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));
        assertThrows(AuthenticationException.class, () -> service.addTraining(validRequest(), "wrong-password"));
    }

    @Test
    void addTraining_throwsEntityNotFound_whenTraineeUnknown() {
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.addTraining(validRequest(), "TrainerPass1!"));
    }

    @Test
    void addTraining_throwsValidationException_onNonPositiveDuration() {
        TrainingCreateRequest request = validRequest();
        request.setTrainingDurationMinutes(0);
        assertThrows(ValidationException.class, () -> service.addTraining(request, "TrainerPass1!"));
    }

    @Test
    void addTraining_throwsValidationException_onMissingTrainingDate() {
        TrainingCreateRequest request = validRequest();
        request.setTrainingDate(null);
        assertThrows(ValidationException.class, () -> service.addTraining(request, "TrainerPass1!"));
    }

    @Test
    void addTraining_throwsValidationException_onNullRequest() {
        assertThrows(ValidationException.class, () -> service.addTraining(null, "TrainerPass1!"));
    }
}
