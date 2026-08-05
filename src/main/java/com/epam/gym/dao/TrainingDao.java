package com.epam.gym.dao;

import com.epam.gym.dto.TraineeTrainingCriteria;
import com.epam.gym.dto.TrainerTrainingCriteria;
import com.epam.gym.entity.Training;
import org.hibernate.Session;

import java.util.List;

public interface TrainingDao {
    Training save(Session session, Training training);
    List<Training> findByTraineeUsername(Session session, String traineeUsername, TraineeTrainingCriteria criteria);
    List<Training> findByTrainerUsername(Session session, String trainerUsername, TrainerTrainingCriteria criteria);
}
