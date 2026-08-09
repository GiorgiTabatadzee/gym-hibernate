package com.epam.gym.service;

import com.epam.gym.dao.UserDao;
import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.util.TransactionExecutor;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final UserDao userDao;
    private final TransactionExecutor transactionExecutor;

    public AuthenticationServiceImpl(UserDao userDao, TransactionExecutor transactionExecutor) {
        this.userDao = userDao;
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public boolean matchCredentials(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return transactionExecutor.executeInTransaction(session ->
                userDao.findByUsername(session, username)
                        .map(user -> user.getPassword().equals(password))
                        .orElse(false));
    }

    @Override
    public void authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null) {
            throw new ValidationException("username and password are required");
        }
        transactionExecutor.executeInTransaction(session -> {
            authenticatedUser(session, username, password);
            return null;
        });
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("newPassword is required");
        }
        transactionExecutor.executeInTransaction(session -> {
            User user = authenticatedUser(session, username, oldPassword);
            user.setPassword(newPassword);
            log.info("Password changed for username={}", username);
            return null;
        });
    }

    private User authenticatedUser(Session session, String username, String password) {
        User user = userDao.findByUsername(session, username).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            log.warn("Authentication failed for username={}", username);
            throw new AuthenticationException("Invalid username or password");
        }
        return user;
    }
}
