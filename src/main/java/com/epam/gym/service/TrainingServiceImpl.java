package com.epam.gym.service;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dto.TrainingCreateRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.util.TransactionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;
    private final TransactionExecutor transactionExecutor;

    public TrainingServiceImpl(TraineeDao traineeDao, TrainerDao trainerDao,
                                TrainingDao trainingDao, TransactionExecutor transactionExecutor) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingDao = trainingDao;
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public Training addTraining(TrainingCreateRequest request, String trainerPassword) {
        validate(request);

        return transactionExecutor.executeInTransaction(session -> {
            Trainer trainer = trainerDao.findByUsername(session, request.getTrainerUsername())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Trainer not found: " + request.getTrainerUsername()));
            if (!trainer.getUser().getPassword().equals(trainerPassword)) {
                log.warn("Authentication failed for trainer username={}", request.getTrainerUsername());
                throw new AuthenticationException("Invalid username or password");
            }

            Trainee trainee = traineeDao.findByUsername(session, request.getTraineeUsername())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Trainee not found: " + request.getTraineeUsername()));

            // Training type follows the assigned trainer's own specialization — the trainer/training
            // relation already fixes it, so it isn't a separate field on the request (task's Add
            // Training request has no training-type field of its own).
            Training training = new Training(trainee, trainer, request.getTrainingName(), trainer.getSpecialization(),
                    request.getTrainingDate(), request.getTrainingDurationMinutes());
            Training saved = trainingDao.save(session, training);

            log.info("Added training id={} for trainee={} with trainer={}",
                    saved.getId(), request.getTraineeUsername(), request.getTrainerUsername());
            return saved;
        });
    }

    private void validate(TrainingCreateRequest request) {
        if (request == null) {
            throw new ValidationException("Training request must not be null");
        }
        requireText("traineeUsername", request.getTraineeUsername());
        requireText("trainerUsername", request.getTrainerUsername());
        requireText("trainingName", request.getTrainingName());
        if (request.getTrainingDate() == null) {
            throw new ValidationException("trainingDate is required");
        }
        if (request.getTrainingDurationMinutes() == null || request.getTrainingDurationMinutes() <= 0) {
            throw new ValidationException("trainingDurationMinutes must be a positive number");
        }
    }

    private void requireText(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
