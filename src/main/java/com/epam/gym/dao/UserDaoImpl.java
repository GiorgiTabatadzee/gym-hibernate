package com.epam.gym.dao;

import com.epam.gym.entity.User;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Optional;

public class UserDaoImpl implements UserDao {

    @Override
    public Optional<User> findByUsername(Session session, String username) {
        Query<User> query = session.createQuery(
                "FROM User u WHERE u.username = :username", User.class);
        query.setParameter("username", username);
        return query.uniqueResultOptional();
    }

    @Override
    public boolean existsByUsername(Session session, String username) {
        Query<Long> query = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class);
        query.setParameter("username", username);
        return query.getSingleResult() > 0;
    }
}
