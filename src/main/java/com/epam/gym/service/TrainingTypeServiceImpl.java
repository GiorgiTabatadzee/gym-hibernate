package com.epam.gym.service;

import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.util.TransactionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TrainingTypeServiceImpl implements TrainingTypeService {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeServiceImpl.class);

    private final TrainingTypeDao trainingTypeDao;
    private final TransactionExecutor transactionExecutor;

    public TrainingTypeServiceImpl(TrainingTypeDao trainingTypeDao, TransactionExecutor transactionExecutor) {
        this.trainingTypeDao = trainingTypeDao;
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public TrainingType create(String trainingTypeName) {
        if (trainingTypeName == null || trainingTypeName.isBlank()) {
            throw new ValidationException("trainingTypeName is required");
        }
        log.info("Creating training type '{}'", trainingTypeName);
        return transactionExecutor.executeInTransaction(session -> {
            if (trainingTypeDao.findByName(session, trainingTypeName).isPresent()) {
                throw new ValidationException("Training type already exists: " + trainingTypeName);
            }
            TrainingType saved = trainingTypeDao.save(session, new TrainingType(trainingTypeName));
            log.debug("Training type created with id {}", saved.getId());
            return saved;
        });
    }

    @Override
    public TrainingType getByName(String trainingTypeName) {
        return transactionExecutor.executeInTransaction(session ->
                trainingTypeDao.findByName(session, trainingTypeName)
                        .orElseThrow(() -> new EntityNotFoundException("Training type not found: " + trainingTypeName)));
    }

    @Override
    public List<TrainingType> getAll() {
        return transactionExecutor.executeInTransaction(session ->
                session.createQuery("FROM TrainingType", TrainingType.class).getResultList());
    }
}
