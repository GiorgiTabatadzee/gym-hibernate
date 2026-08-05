package com.epam.gym.dao;

import com.epam.gym.entity.TrainingType;
import org.hibernate.Session;

import java.util.Optional;

public interface TrainingTypeDao {
    TrainingType save(Session session, TrainingType trainingType);
    Optional<TrainingType> findByName(Session session, String name);
    Optional<TrainingType> findById(Session session, Long id);
}
