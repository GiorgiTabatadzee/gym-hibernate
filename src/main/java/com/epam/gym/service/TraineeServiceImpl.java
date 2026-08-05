package com.epam.gym.service;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.UserDao;
import com.epam.gym.dto.TraineeTrainingCriteria;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.IllegalStateTransitionException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.util.CredentialGenerator;
import com.epam.gym.util.TransactionExecutor;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TraineeServiceImpl implements TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;
    private final UserDao userDao;
    private final TransactionExecutor transactionExecutor;

    public TraineeServiceImpl(TraineeDao traineeDao, TrainerDao trainerDao, TrainingDao trainingDao,
                               UserDao userDao, TransactionExecutor transactionExecutor) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingDao = trainingDao;
        this.userDao = userDao;
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public Trainee createTraineeProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        validateRequired("firstName", firstName);
        validateRequired("lastName", lastName);

        return transactionExecutor.executeInTransaction(session -> {
            String username = CredentialGenerator.generateUsername(
                    firstName, lastName, candidate -> userDao.existsByUsername(session, candidate));
            String password = CredentialGenerator.generatePassword();

            User user = new User(firstName, lastName, username, password, true);
            Trainee trainee = new Trainee(dateOfBirth, address, user);
            Trainee saved = traineeDao.save(session, trainee);

            log.info("Created trainee profile: username={}, id={}", username, saved.getId());
            return saved;
        });
    }

    @Override
    public boolean matchCredentials(String username, String password) {
        return transactionExecutor.executeInTransaction(session ->
                traineeDao.findByUsername(session, username)
                        .map(t -> t.getUser().getPassword().equals(password))
                        .orElse(false));
    }

    @Override
    public Trainee getProfileByUsername(String username, String password) {
        return transactionExecutor.executeInTransaction(session -> {
            Trainee trainee = authenticate(session, username, password);
            Hibernate.initialize(trainee.getTrainers());
            Hibernate.initialize(trainee.getTrainings());
            return trainee;
        });
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        validateRequired("newPassword", newPassword);
        transactionExecutor.executeInTransaction(session -> {
            Trainee trainee = authenticate(session, username, oldPassword);
            trainee.getUser().setPassword(newPassword);
            log.info("Password changed for trainee username={}", username);
            return null;
        });
    }

    @Override
    public Trainee updateProfile(String username, String password, String firstName, String lastName,
                                  LocalDate dateOfBirth, String address) {
        validateRequired("firstName", firstName);
        validateRequired("lastName", lastName);

        return transactionExecutor.executeInTransaction(session -> {
            Trainee trainee = authenticate(session, username, password);
            trainee.getUser().setFirstName(firstName);
            trainee.getUser().setLastName(lastName);
            trainee.setDateOfBirth(dateOfBirth);
            trainee.setAddress(address);
            log.info("Updated trainee profile: username={}", username);
            return trainee;
        });
    }

    @Override
    public void setActive(String username, String password, boolean active) {
        transactionExecutor.executeInTransaction(session -> {
            Trainee trainee = authenticate(session, username, password);
            boolean currentlyActive = Boolean.TRUE.equals(trainee.getUser().getIsActive());
            if (currentlyActive == active) {
                throw new IllegalStateTransitionException(
                        "Trainee '" + username + "' is already " + (active ? "active" : "inactive"));
            }
            trainee.getUser().setIsActive(active);
            log.info("Trainee username={} set to active={}", username, active);
            return null;
        });
    }

    @Override
    public void deleteProfileByUsername(String username, String password) {
        transactionExecutor.executeInTransaction(session -> {
            Trainee trainee = authenticate(session, username, password);
            traineeDao.delete(session, trainee);
            log.info("Deleted trainee profile (hard delete, cascades trainings): username={}", username);
            return null;
        });
    }

    @Override
    public List<Training> getTrainingsList(String username, String password, TraineeTrainingCriteria criteria) {
        return transactionExecutor.executeInTransaction(session -> {
            authenticate(session, username, password);
            return trainingDao.findByTraineeUsername(session, username, criteria);
        });
    }

    @Override
    public List<Trainer> getTrainersNotAssigned(String username, String password) {
        return transactionExecutor.executeInTransaction(session -> {
            authenticate(session, username, password);
            return traineeDao.findTrainersNotAssigned(session, username);
        });
    }

    @Override
    public void updateTrainersList(String username, String password, List<String> trainerUsernames) {
        if (trainerUsernames == null) {
            throw new ValidationException("trainerUsernames list must not be null");
        }
        transactionExecutor.executeInTransaction(session -> {
            Trainee trainee = authenticate(session, username, password);
            Set<Trainer> newTrainers = new HashSet<>();
            for (String trainerUsername : trainerUsernames) {
                Trainer trainer = trainerDao.findByUsername(session, trainerUsername)
                        .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerUsername));
                newTrainers.add(trainer);
            }
            trainee.setTrainers(newTrainers);
            log.info("Updated trainers list for trainee username={}, count={}", username, newTrainers.size());
            return null;
        });
    }

    private Trainee authenticate(Session session, String username, String password) {
        Trainee trainee = traineeDao.findByUsername(session, username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
        if (!trainee.getUser().getPassword().equals(password)) {
            log.warn("Authentication failed for trainee username={}", username);
            throw new AuthenticationException("Invalid username or password");
        }
        return trainee;
    }

    private void validateRequired(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
