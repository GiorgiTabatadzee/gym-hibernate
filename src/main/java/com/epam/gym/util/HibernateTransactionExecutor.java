package com.epam.gym.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Production TransactionExecutor: opens a Session, begins a Transaction, runs
 * the given unit of work, commits on success, rolls back and rethrows on any
 * RuntimeException. Every service method funnels through this single place,
 * so transaction boundaries are consistent and never left half-open.
 */
public class HibernateTransactionExecutor implements TransactionExecutor {

    private static final Logger log = LoggerFactory.getLogger(HibernateTransactionExecutor.class);

    private final SessionFactory sessionFactory;

    public HibernateTransactionExecutor(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public <T> T executeInTransaction(Function<Session, T> work) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T result = work.apply(session);
                tx.commit();
                return result;
            } catch (RuntimeException e) {
                log.warn("Rolling back transaction due to: {}", e.getMessage());
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }
}
