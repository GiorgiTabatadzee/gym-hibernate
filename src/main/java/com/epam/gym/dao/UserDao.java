package com.epam.gym.dao;

import com.epam.gym.entity.User;
import org.hibernate.Session;

import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(Session session, String username);
    boolean existsByUsername(Session session, String username);
}
