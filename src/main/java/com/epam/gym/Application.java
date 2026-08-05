package com.epam.gym;

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
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TraineeServiceImpl;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainerServiceImpl;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.TrainingServiceImpl;
import com.epam.gym.service.TrainingTypeService;
import com.epam.gym.service.TrainingTypeServiceImpl;
import com.epam.gym.util.HibernateTransactionExecutor;
import com.epam.gym.util.HibernateUtil;
import com.epam.gym.util.TransactionExecutor;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * Manual smoke-test / demo driver: walks through every one of the 18 required
 * functionalities against a real (H2) database so the whole stack can be
 * exercised end to end with `mvn compile exec:java` or by running this class
 * directly from an IDE. Not a substitute for the unit tests under src/test.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        TransactionExecutor tx = new HibernateTransactionExecutor(sessionFactory);

        TrainingTypeService trainingTypeService = new TrainingTypeServiceImpl(new TrainingTypeDaoImpl(), tx);
        TraineeService traineeService = new TraineeServiceImpl(
                new TraineeDaoImpl(), new TrainerDaoImpl(), new TrainingDaoImpl(), new UserDaoImpl(), tx);
        TrainerService trainerService = new TrainerServiceImpl(
                new TrainerDaoImpl(), new TrainingTypeDaoImpl(), new TrainingDaoImpl(), new UserDaoImpl(), tx);
        TrainingService trainingService = new TrainingServiceImpl(
                new TraineeDaoImpl(), new TrainerDaoImpl(), new TrainingTypeDaoImpl(), new TrainingDaoImpl(), tx);

        try {
            // Seed the constant training-type list (idempotent-ish for repeat runs).
            TrainingType cardio = getOrCreate(trainingTypeService, "Cardio");

            // 1 & 2: create profiles.
            Trainer trainer = trainerService.createTrainerProfile("Nino", "Kapanadze", "Cardio");
            Trainee trainee = traineeService.createTraineeProfile(
                    "Giorgi", "Beridze", LocalDate.of(2000, 5, 20), "12 Rustaveli Ave, Tbilisi");
            log.info("Generated trainer credentials: username={}", trainer.getUser().getUsername());
            log.info("Generated trainee credentials: username={}", trainee.getUser().getUsername());

            String trainerUsername = trainer.getUser().getUsername();
            String trainerPassword = trainer.getUser().getPassword();
            String traineeUsername = trainee.getUser().getUsername();
            String traineePassword = trainee.getUser().getPassword();

            // 3 & 4: credential matching.
            log.info("Trainee credentials match: {}", traineeService.matchCredentials(traineeUsername, traineePassword));
            log.info("Trainer credentials match: {}", trainerService.matchCredentials(trainerUsername, trainerPassword));

            // 5 & 6: select profiles.
            traineeService.getProfileByUsername(traineeUsername, traineePassword);
            trainerService.getProfileByUsername(trainerUsername, trainerPassword);

            // 7 & 8: password change.
            traineeService.changePassword(traineeUsername, traineePassword, "NewTraineePass1");
            traineePassword = "NewTraineePass1";
            trainerService.changePassword(trainerUsername, trainerPassword, "NewTrainerPass1");
            trainerPassword = "NewTrainerPass1";

            // 9 & 10: update profiles.
            trainerService.updateProfile(trainerUsername, trainerPassword, "Nino", "Kapanadze-Gelashvili");
            traineeService.updateProfile(traineeUsername, traineePassword, "Giorgi", "Beridze",
                    LocalDate.of(2000, 5, 20), "45 Chavchavadze Ave, Tbilisi");

            // 11 & 12: activate/de-activate (not idempotent).
            traineeService.setActive(traineeUsername, traineePassword, false);
            traineeService.setActive(traineeUsername, traineePassword, true);
            trainerService.setActive(trainerUsername, trainerPassword, false);
            trainerService.setActive(trainerUsername, trainerPassword, true);

            // 16: add a training.
            TrainingCreateRequest request = new TrainingCreateRequest();
            request.setTraineeUsername(traineeUsername);
            request.setTrainerUsername(trainerUsername);
            request.setTrainingName("Morning Cardio Session");
            request.setTrainingTypeName(cardio.getTrainingTypeName());
            request.setTrainingDate(LocalDate.now());
            request.setTrainingDurationMinutes(45);
            Training training = trainingService.addTraining(request, trainerPassword);
            log.info("Added training: {}", training);

            // 14 & 15: trainings lists.
            List<Training> traineeTrainings = traineeService.getTrainingsList(
                    traineeUsername, traineePassword, new TraineeTrainingCriteria());
            log.info("Trainee trainings count: {}", traineeTrainings.size());
            List<Training> trainerTrainings = trainerService.getTrainingsList(
                    trainerUsername, trainerPassword, null);
            log.info("Trainer trainings count: {}", trainerTrainings.size());

            // 17 & 18: trainers list management.
            List<Trainer> notAssigned = traineeService.getTrainersNotAssigned(traineeUsername, traineePassword);
            log.info("Trainers not yet assigned to trainee: {}", notAssigned.size());
            traineeService.updateTrainersList(traineeUsername, traineePassword, List.of(trainerUsername));

            // 13: hard delete (cascades trainings).
            traineeService.deleteProfileByUsername(traineeUsername, traineePassword);
            log.info("Demo run completed successfully.");

        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static TrainingType getOrCreate(TrainingTypeService service, String name) {
        try {
            return service.getByName(name);
        } catch (RuntimeException e) {
            return service.create(name);
        }
    }
}
