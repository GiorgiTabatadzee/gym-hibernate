package com.epam.gym.dao;

import com.epam.gym.entity.Trainer;
import org.hibernate.Session;

import java.util.Optional;

public interface TrainerDao {
    Trainer save(Session session, Trainer trainer);
    Optional<Trainer> findByUsername(Session session, String username);
}
