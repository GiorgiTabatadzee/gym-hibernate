package com.epam.gym.dao;

import com.epam.gym.dto.TraineeTrainingCriteria;
import com.epam.gym.dto.TrainerTrainingCriteria;
import com.epam.gym.entity.Training;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class TrainingDaoImpl implements TrainingDao {

    @Override
    public Training save(Session session, Training training) {
        session.persist(training);
        return training;
    }

    @Override
    public List<Training> findByTraineeUsername(Session session, String traineeUsername,
                                                  TraineeTrainingCriteria criteria) {
        StringBuilder hql = new StringBuilder(
                "FROM Training t JOIN FETCH t.trainer tr JOIN FETCH tr.user JOIN FETCH t.trainingType " +
                        "WHERE t.trainee.user.username = :username");
        if (criteria != null) {
            if (criteria.getFromDate() != null) hql.append(" AND t.trainingDate >= :fromDate");
            if (criteria.getToDate() != null) hql.append(" AND t.trainingDate <= :toDate");
            if (criteria.getTrainerName() != null) hql.append(" AND t.trainer.user.lastName = :trainerName");
            if (criteria.getTrainingTypeName() != null) hql.append(" AND t.trainingType.trainingTypeName = :typeName");
        }
        Query<Training> query = session.createQuery(hql.toString(), Training.class);
        query.setParameter("username", traineeUsername);
        if (criteria != null) {
            if (criteria.getFromDate() != null) query.setParameter("fromDate", criteria.getFromDate());
            if (criteria.getToDate() != null) query.setParameter("toDate", criteria.getToDate());
            if (criteria.getTrainerName() != null) query.setParameter("trainerName", criteria.getTrainerName());
            if (criteria.getTrainingTypeName() != null) query.setParameter("typeName", criteria.getTrainingTypeName());
        }
        return query.getResultList();
    }

    @Override
    public List<Training> findByTrainerUsername(Session session, String trainerUsername,
                                                  TrainerTrainingCriteria criteria) {
        StringBuilder hql = new StringBuilder(
                "FROM Training t JOIN FETCH t.trainee tn JOIN FETCH tn.user JOIN FETCH t.trainingType " +
                        "WHERE t.trainer.user.username = :username");
        if (criteria != null) {
            if (criteria.getFromDate() != null) hql.append(" AND t.trainingDate >= :fromDate");
            if (criteria.getToDate() != null) hql.append(" AND t.trainingDate <= :toDate");
            if (criteria.getTraineeName() != null) hql.append(" AND t.trainee.user.lastName = :traineeName");
        }
        Query<Training> query = session.createQuery(hql.toString(), Training.class);
        query.setParameter("username", trainerUsername);
        if (criteria != null) {
            if (criteria.getFromDate() != null) query.setParameter("fromDate", criteria.getFromDate());
            if (criteria.getToDate() != null) query.setParameter("toDate", criteria.getToDate());
            if (criteria.getTraineeName() != null) query.setParameter("traineeName", criteria.getTraineeName());
        }
        return query.getResultList();
    }
}
