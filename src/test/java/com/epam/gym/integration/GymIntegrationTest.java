package com.epam.gym.integration;

import com.epam.gym.dao.TraineeDaoImpl;
import com.epam.gym.dao.TrainerDaoImpl;
import com.epam.gym.dao.TrainingDaoImpl;
import com.epam.gym.dao.TrainingTypeDaoImpl;
import com.epam.gym.dao.UserDaoImpl;
import com.epam.gym.dto.TraineeTrainingCriteria;
import com.epam.gym.dto.TrainingCreateRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.IllegalStateTransitionException;
import com.epam.gym.service.TraineeServiceImpl;
import com.epam.gym.service.TrainerServiceImpl;
import com.epam.gym.service.TrainingServiceImpl;
import com.epam.gym.service.TrainingTypeServiceImpl;
import com.epam.gym.util.HibernateTransactionExecutor;
import com.epam.gym.util.HibernateUtil;
import com.epam.gym.util.TransactionExecutor;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end test against a real (in-memory) H2 database via actual Hibernate
 * Sessions and Transactions — verifies the entity mappings, generated schema,
 * and cascade behavior actually work, not just the service-layer logic that
 * the mocked unit tests cover.
 */
class GymIntegrationTest {

    private static TraineeServiceImpl traineeService;
    private static TrainerServiceImpl trainerService;
    private static TrainingServiceImpl trainingService;
    private static TrainingTypeServiceImpl trainingTypeService;

    @BeforeAll
    static void setUpDatabase() {
        SessionFactory sessionFactory = HibernateUtil.rebuild("hibernate-test.cfg.xml");
        TransactionExecutor tx = new HibernateTransactionExecutor(sessionFactory);

        trainingTypeService = new TrainingTypeServiceImpl(new TrainingTypeDaoImpl(), tx);
        traineeService = new TraineeServiceImpl(
                new TraineeDaoImpl(), new TrainerDaoImpl(), new TrainingDaoImpl(), new UserDaoImpl(), tx);
        trainerService = new TrainerServiceImpl(
                new TrainerDaoImpl(), new TrainingTypeDaoImpl(), new TrainingDaoImpl(), new UserDaoImpl(), tx);
        trainingService = new TrainingServiceImpl(
                new TraineeDaoImpl(), new TrainerDaoImpl(), new TrainingDaoImpl(), tx);
    }

    @AfterAll
    static void tearDownDatabase() {
        HibernateUtil.shutdown();
    }

    @Test
    void fullLifecycle_createAuthenticateTrainDeleteCascades() {
        // Seed reference data.
        TrainingType cardio = trainingTypeService.create("Cardio");

        // 1 & 2: create profiles with generated credentials.
        Trainer trainer = trainerService.createTrainerProfile("Nino", "Kapanadze", "Cardio");
        Trainee trainee = traineeService.createTraineeProfile(
                "Giorgi", "Beridze", LocalDate.of(2000, 1, 1), "Tbilisi");

        String trainerUsername = trainer.getUser().getUsername();
        String trainerPassword = trainer.getUser().getPassword();
        String traineeUsername = trainee.getUser().getUsername();
        String traineePassword = trainee.getUser().getPassword();

        // 3 & 4: credentials match against what was actually persisted.
        assertTrue(traineeService.matchCredentials(traineeUsername, traineePassword));
        assertTrue(trainerService.matchCredentials(trainerUsername, trainerPassword));

        // 11: activate/de-activate is not idempotent.
        assertThrows(IllegalStateTransitionException.class,
                () -> traineeService.setActive(traineeUsername, traineePassword, true));
        traineeService.setActive(traineeUsername, traineePassword, false);
        traineeService.setActive(traineeUsername, traineePassword, true);

        // 16: add a training linking the two persisted profiles.
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTraineeUsername(traineeUsername);
        request.setTrainerUsername(trainerUsername);
        request.setTrainingName("Morning Cardio Session");
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDurationMinutes(30);
        trainingService.addTraining(request, trainerPassword);

        // 14: trainings list is retrievable through the trainee.
        List<Training> trainings = traineeService.getTrainingsList(
                traineeUsername, traineePassword, new TraineeTrainingCriteria());
        assertEquals(1, trainings.size());
        assertEquals("Morning Cardio Session", trainings.get(0).getTrainingName());
        assertEquals(cardio.getTrainingTypeName(), trainings.get(0).getTrainingType().getTrainingTypeName());

        // 18: assign the trainer to the trainee's trainers list, then verify #17 excludes them.
        traineeService.updateTrainersList(traineeUsername, traineePassword, List.of(trainerUsername));
        List<Trainer> notAssigned = traineeService.getTrainersNotAssigned(traineeUsername, traineePassword);
        assertTrue(notAssigned.stream().noneMatch(t -> t.getUser().getUsername().equals(trainerUsername)));

        // 13: hard delete cascades to the trainee's trainings.
        traineeService.deleteProfileByUsername(traineeUsername, traineePassword);
        assertThrows(EntityNotFoundException.class,
                () -> traineeService.getProfileByUsername(traineeUsername, traineePassword));

        // The trainer's own trainings list must be unaffected by the trainee's deletion at the DAO level;
        // only the now-orphaned Training row itself was removed by the cascade.
        List<Training> trainerTrainingsAfterDelete = trainerService.getTrainingsList(trainerUsername, trainerPassword, null);
        assertEquals(0, trainerTrainingsAfterDelete.size());
    }
}
