package com.epam.gym.service;

import com.epam.gym.entity.TrainingType;

import java.util.List;

/** TrainingType is a constant reference list — create/read only, no update (task note #12). */
public interface TrainingTypeService {
    TrainingType create(String trainingTypeName);
    TrainingType getByName(String trainingTypeName);
    List<TrainingType> getAll();
}
