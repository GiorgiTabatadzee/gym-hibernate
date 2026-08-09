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
import com.epam.gym.util.CredentialGenerator;
import com.epam.gym.util.TransactionExecutor;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TrainerServiceImpl implements TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final TrainingDao trainingDao;
    private final UserDao userDao;
    private final TransactionExecutor transactionExecutor;

    public TrainerServiceImpl(TrainerDao trainerDao, TrainingTypeDao trainingTypeDao, TrainingDao trainingDao,
                               UserDao userDao, TransactionExecutor transactionExecutor) {
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.trainingDao = trainingDao;
        this.userDao = userDao;
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public Trainer createTrainerProfile(String firstName, String lastName, String specializationName) {
        validateRequired("firstName", firstName);
        validateRequired("lastName", lastName);
        validateRequired("specializationName", specializationName);

        return transactionExecutor.executeInTransaction(session -> {
            TrainingType specialization = trainingTypeDao.findByName(session, specializationName)
                    .orElseThrow(() -> new EntityNotFoundException("Unknown specialization: " + specializationName));

            String username = CredentialGenerator.generateUsername(
                    firstName, lastName, candidate -> userDao.existsByUsername(session, candidate));
            String password = CredentialGenerator.generatePassword();

            User user = new User(firstName, lastName, username, password, true);
            Trainer trainer = new Trainer(specialization, user);
            Trainer saved = trainerDao.save(session, trainer);

            log.info("Created trainer profile: username={}, id={}", username, saved.getId());
            return saved;
        });
    }

    @Override
    public boolean matchCredentials(String username, String password) {
        return transactionExecutor.executeInTransaction(session ->
                trainerDao.findByUsername(session, username)
                        .map(t -> t.getUser().getPassword().equals(password))
                        .orElse(false));
    }

    @Override
    public Trainer getProfileByUsername(String username, String password) {
        return transactionExecutor.executeInTransaction(session -> {
            Trainer trainer = authenticate(session, username, password);
            Hibernate.initialize(trainer.getSpecialization());
            Hibernate.initialize(trainer.getTrainees());
            Hibernate.initialize(trainer.getTrainings());
            return trainer;
        });
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        validateRequired("newPassword", newPassword);
        transactionExecutor.executeInTransaction(session -> {
            Trainer trainer = authenticate(session, username, oldPassword);
            trainer.getUser().setPassword(newPassword);
            log.info("Password changed for trainer username={}", username);
            return null;
        });
    }

    @Override
    public Trainer updateProfile(String username, String password, String firstName, String lastName,
                                  boolean isActive) {
        validateRequired("firstName", firstName);
        validateRequired("lastName", lastName);

        return transactionExecutor.executeInTransaction(session -> {
            Trainer trainer = authenticate(session, username, password);
            trainer.getUser().setFirstName(firstName);
            trainer.getUser().setLastName(lastName);
            trainer.getUser().setIsActive(isActive);
            Hibernate.initialize(trainer.getSpecialization());
            Hibernate.initialize(trainer.getTrainees());
            log.info("Updated trainer profile: username={}", username);
            return trainer;
        });
    }

    @Override
    public void setActive(String username, String password, boolean active) {
        transactionExecutor.executeInTransaction(session -> {
            Trainer trainer = authenticate(session, username, password);
            boolean currentlyActive = Boolean.TRUE.equals(trainer.getUser().getIsActive());
            if (currentlyActive == active) {
                throw new IllegalStateTransitionException(
                        "Trainer '" + username + "' is already " + (active ? "active" : "inactive"));
            }
            trainer.getUser().setIsActive(active);
            log.info("Trainer username={} set to active={}", username, active);
            return null;
        });
    }

    @Override
    public List<Training> getTrainingsList(String username, String password, TrainerTrainingCriteria criteria) {
        return transactionExecutor.executeInTransaction(session -> {
            authenticate(session, username, password);
            return trainingDao.findByTrainerUsername(session, username, criteria);
        });
    }

    private Trainer authenticate(Session session, String username, String password) {
        Trainer trainer = trainerDao.findByUsername(session, username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));
        if (!trainer.getUser().getPassword().equals(password)) {
            log.warn("Authentication failed for trainer username={}", username);
            throw new AuthenticationException("Invalid username or password");
        }
        return trainer;
    }

    private void validateRequired(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
