package com.epam.gym.service;

import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.dao.UserDao;
import com.epam.gym.dto.TrainerTrainingCriteria;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerDao trainerDao;
    @Mock
    private TrainingTypeDao trainingTypeDao;
    @Mock
    private TrainingDao trainingDao;
    @Mock
    private UserDao userDao;

    private TrainerServiceImpl service;
    private Trainer trainer;
    private User user;
    private TrainingType cardio;

    @BeforeEach
    void setUp() {
        service = new TrainerServiceImpl(trainerDao, trainingTypeDao, trainingDao, userDao, new FakeTransactionExecutor());
        user = new User("Nino", "Kapanadze", "nino.kapanadze", "Secret123!", true);
        cardio = new TrainingType("Cardio");
        cardio.setId(1L);
        trainer = new Trainer(cardio, user);
        trainer.setId(1L);
        user.setId(1L);
    }

    @Test
    void createTrainerProfile_generatesUsernameAndAssignsSpecialization() {
        when(trainingTypeDao.findByName(any(Session.class), eq("Cardio"))).thenReturn(Optional.of(cardio));
        when(userDao.existsByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(false);
        when(trainerDao.save(any(Session.class), any(Trainer.class))).thenAnswer(inv -> inv.getArgument(1));

        Trainer created = service.createTrainerProfile("Nino", "Kapanadze", "Cardio");

        assertEquals("nino.kapanadze", created.getUser().getUsername());
        assertEquals("Cardio", created.getSpecialization().getTrainingTypeName());
    }

    @Test
    void createTrainerProfile_throwsEntityNotFound_forUnknownSpecialization() {
        when(trainingTypeDao.findByName(any(Session.class), eq("Underwater Basket Weaving")))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.createTrainerProfile("Nino", "Kapanadze", "Underwater Basket Weaving"));
    }

    @Test
    void createTrainerProfile_throwsOnBlankSpecialization() {
        assertThrows(ValidationException.class,
                () -> service.createTrainerProfile("Nino", "Kapanadze", " "));
    }

    @Test
    void matchCredentials_returnsTrueOnCorrectPassword() {
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));
        assertTrue(service.matchCredentials("nino.kapanadze", "Secret123!"));
    }

    @Test
    void matchCredentials_returnsFalseOnWrongPassword() {
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));
        assertFalse(service.matchCredentials("nino.kapanadze", "wrong"));
    }

    @Test
    void changePassword_throwsAuthenticationException_onWrongOldPassword() {
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));
        assertThrows(AuthenticationException.class,
                () -> service.changePassword("nino.kapanadze", "wrong-old-password", "NewPass1!"));
    }

    @Test
    void changePassword_updatesPassword_onCorrectOldPassword() {
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));
        service.changePassword("nino.kapanadze", "Secret123!", "NewPass1!");
        assertEquals("NewPass1!", trainer.getUser().getPassword());
    }

    @Test
    void updateProfile_updatesFirstAndLastNameAndActiveState() {
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));
        Trainer updated = service.updateProfile("nino.kapanadze", "Secret123!", "Nino", "Gelashvili", false);
        assertEquals("Gelashvili", updated.getUser().getLastName());
        assertFalse(updated.getUser().getIsActive());
    }

    @Test
    void setActive_throwsIllegalStateTransition_whenAlreadyInactive() {
        user.setIsActive(false);
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));
        assertThrows(IllegalStateTransitionException.class,
                () -> service.setActive("nino.kapanadze", "Secret123!", false));
    }

    @Test
    void setActive_succeeds_whenTransitioningState() {
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));
        service.setActive("nino.kapanadze", "Secret123!", false);
        assertFalse(trainer.getUser().getIsActive());
    }

    @Test
    void getTrainingsList_delegatesToDao_afterAuthentication() {
        when(trainerDao.findByUsername(any(Session.class), eq("nino.kapanadze"))).thenReturn(Optional.of(trainer));
        when(trainingDao.findByTrainerUsername(any(Session.class), eq("nino.kapanadze"), any(TrainerTrainingCriteria.class)))
                .thenReturn(List.of(new Training()));

        List<Training> result = service.getTrainingsList("nino.kapanadze", "Secret123!", new TrainerTrainingCriteria());

        assertEquals(1, result.size());
    }

    @Test
    void getTrainingsList_throwsEntityNotFound_whenTrainerUnknown() {
        when(trainerDao.findByUsername(any(Session.class), eq("ghost"))).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> service.getTrainingsList("ghost", "x", null));
    }
}
