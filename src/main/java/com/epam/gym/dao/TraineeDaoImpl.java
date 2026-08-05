package com.epam.gym.dao;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class TraineeDaoImpl implements TraineeDao {

    @Override
    public Trainee save(Session session, Trainee trainee) {
        session.persist(trainee);
        return trainee;
    }

    @Override
    public Optional<Trainee> findByUsername(Session session, String username) {
        Query<Trainee> query = session.createQuery(
                "FROM Trainee t JOIN FETCH t.user WHERE t.user.username = :username", Trainee.class);
        query.setParameter("username", username);
        return query.uniqueResultOptional();
    }

    @Override
    public void delete(Session session, Trainee trainee) {
        session.remove(trainee);
    }

    @Override
    public List<Trainer> findTrainersNotAssigned(Session session, String traineeUsername) {
        Query<Trainer> query = session.createQuery(
                "FROM Trainer tr WHERE tr.id NOT IN " +
                        "(SELECT t.id FROM Trainee trn JOIN trn.trainers t WHERE trn.user.username = :username)",
                Trainer.class);
        query.setParameter("username", traineeUsername);
        return query.getResultList();
    }
}
