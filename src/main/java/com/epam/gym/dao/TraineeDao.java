package com.epam.gym.dao;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee save(Session session, Trainee trainee);
    Optional<Trainee> findByUsername(Session session, String username);
    void delete(Session session, Trainee trainee);
    List<Trainer> findTrainersNotAssigned(Session session, String traineeUsername);
}
