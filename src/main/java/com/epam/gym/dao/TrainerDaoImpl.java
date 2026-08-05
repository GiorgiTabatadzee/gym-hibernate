package com.epam.gym.dao;

import com.epam.gym.entity.Trainer;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Optional;

public class TrainerDaoImpl implements TrainerDao {

    @Override
    public Trainer save(Session session, Trainer trainer) {
        session.persist(trainer);
        return trainer;
    }

    @Override
    public Optional<Trainer> findByUsername(Session session, String username) {
        Query<Trainer> query = session.createQuery(
                "FROM Trainer t JOIN FETCH t.user WHERE t.user.username = :username", Trainer.class);
        query.setParameter("username", username);
        return query.uniqueResultOptional();
    }
}
