package com.epam.gym.dao;

import com.epam.gym.entity.TrainingType;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Optional;

public class TrainingTypeDaoImpl implements TrainingTypeDao {

    @Override
    public TrainingType save(Session session, TrainingType trainingType) {
        session.persist(trainingType);
        return trainingType;
    }

    @Override
    public Optional<TrainingType> findByName(Session session, String name) {
        Query<TrainingType> query = session.createQuery(
                "FROM TrainingType t WHERE t.trainingTypeName = :name", TrainingType.class);
        query.setParameter("name", name);
        return query.uniqueResultOptional();
    }

    @Override
    public Optional<TrainingType> findById(Session session, Long id) {
        return Optional.ofNullable(session.find(TrainingType.class, id));
    }
}
