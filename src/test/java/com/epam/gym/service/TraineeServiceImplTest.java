package com.epam.gym.service;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.UserDao;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.IllegalStateTransitionException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.util.FakeTransactionExecutor;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private TrainingDao trainingDao;
    @Mock
    private UserDao userDao;

    private TraineeServiceImpl service;
    private Trainee trainee;
    private User user;

    @BeforeEach
    void setUp() {
        service = new TraineeServiceImpl(traineeDao, trainerDao, trainingDao, userDao, new FakeTransactionExecutor());
        user = new User("Giorgi", "Beridze", "giorgi.beridze", "Secret123!", true);
        trainee = new Trainee(LocalDate.of(2000, 1, 1), "Tbilisi", user);
        trainee.setId(1L);
        user.setId(1L);
    }

    @Test
    void createTraineeProfile_generatesUsernameFromNameWhenAvailable() {
        when(userDao.existsByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(false);
        when(traineeDao.save(any(Session.class), any(Trainee.class))).thenAnswer(inv -> inv.getArgument(1));

        Trainee created = service.createTraineeProfile("Giorgi", "Beridze", LocalDate.of(2000, 1, 1), "Tbilisi");

        assertEquals("giorgi.beridze", created.getUser().getUsername());
        assertEquals(10, created.getUser().getPassword().length());
        assertTrue(created.getUser().getIsActive());
    }

    @Test
    void createTraineeProfile_appendsSerialOnUsernameCollision() {
        when(userDao.existsByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(true);
        when(userDao.existsByUsername(any(Session.class), eq("giorgi.beridze1"))).thenReturn(false);
        when(traineeDao.save(any(Session.class), any(Trainee.class))).thenAnswer(inv -> inv.getArgument(1));

        Trainee created = service.createTraineeProfile("Giorgi", "Beridze", LocalDate.of(2000, 1, 1), "Tbilisi");

        assertEquals("giorgi.beridze1", created.getUser().getUsername());
    }

    @Test
    void createTraineeProfile_throwsOnBlankFirstName() {
        assertThrows(ValidationException.class,
                () -> service.createTraineeProfile("  ", "Beridze", LocalDate.now(), "Tbilisi"));
    }

    @Test
    void matchCredentials_returnsTrueOnCorrectPassword() {
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        assertTrue(service.matchCredentials("giorgi.beridze", "Secret123!"));
    }

    @Test
    void matchCredentials_returnsFalseOnWrongPassword() {
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        assertFalse(service.matchCredentials("giorgi.beridze", "wrong-password"));
    }

    @Test
    void matchCredentials_returnsFalseWhenUsernameUnknown() {
        when(traineeDao.findByUsername(any(Session.class), eq("nobody"))).thenReturn(Optional.empty());
        assertFalse(service.matchCredentials("nobody", "anything"));
    }

    @Test
    void getProfileByUsername_throwsAuthenticationException_onWrongPassword() {
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        assertThrows(AuthenticationException.class,
                () -> service.getProfileByUsername("giorgi.beridze", "wrong-password"));
    }

    @Test
    void getProfileByUsername_throwsEntityNotFound_whenUsernameUnknown() {
        when(traineeDao.findByUsername(any(Session.class), eq("nobody"))).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getProfileByUsername("nobody", "x"));
    }

    @Test
    void changePassword_updatesUserPassword_afterSuccessfulAuthentication() {
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        service.changePassword("giorgi.beridze", "Secret123!", "NewSecret456!");
        assertEquals("NewSecret456!", trainee.getUser().getPassword());
    }

    @Test
    void changePassword_throwsOnBlankNewPassword() {
        assertThrows(ValidationException.class,
                () -> service.changePassword("giorgi.beridze", "Secret123!", " "));
    }

    @Test
    void setActive_throwsIllegalStateTransition_whenAlreadyActive() {
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        assertThrows(IllegalStateTransitionException.class,
                () -> service.setActive("giorgi.beridze", "Secret123!", true));
    }

    @Test
    void setActive_succeeds_whenTransitioningToOppositeState() {
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        service.setActive("giorgi.beridze", "Secret123!", false);
        assertFalse(trainee.getUser().getIsActive());
    }

    @Test
    void deleteProfileByUsername_delegatesToDao_afterAuthentication() {
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        service.deleteProfileByUsername("giorgi.beridze", "Secret123!");
        verify(traineeDao, times(1)).delete(any(Session.class), eq(trainee));
    }

    @Test
    void deleteProfileByUsername_doesNotDelete_whenAuthenticationFails() {
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        assertThrows(AuthenticationException.class,
                () -> service.deleteProfileByUsername("giorgi.beridze", "wrong-password"));
        verify(traineeDao, times(0)).delete(any(Session.class), any(Trainee.class));
    }

    @Test
    void getTrainersNotAssigned_delegatesToDao_afterAuthentication() {
        Trainer trainer = new Trainer();
        trainer.setId(2L);
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        when(traineeDao.findTrainersNotAssigned(any(Session.class), eq("giorgi.beridze")))
                .thenReturn(List.of(trainer));

        List<Trainer> result = service.getTrainersNotAssigned("giorgi.beridze", "Secret123!");

        assertEquals(1, result.size());
    }

    @Test
    void updateTrainersList_throwsEntityNotFound_whenTrainerUsernameUnknown() {
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(any(Session.class), eq("ghost.trainer"))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                service.updateTrainersList("giorgi.beridze", "Secret123!", List.of("ghost.trainer")));
    }

    @Test
    void updateTrainersList_replacesTraineeTrainerSet() {
        Trainer trainer = mock(Trainer.class);
        when(traineeDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));

        service.updateTrainersList("giorgi.beridze", "Secret123!", List.of("nino.kapanadze"));

        assertEquals(Set.of(trainer), trainee.getTrainers());
    }
}
