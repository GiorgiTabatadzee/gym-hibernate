package com.epam.gym.service;

import com.epam.gym.dto.TrainingCreateRequest;
import com.epam.gym.entity.Training;

public interface TrainingService {
    /** #16 Add training. Authenticated as the assigned trainer. */
    Training addTraining(TrainingCreateRequest request, String trainerPassword);
}
