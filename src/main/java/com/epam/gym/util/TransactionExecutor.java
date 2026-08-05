package com.epam.gym.util;

import org.hibernate.Session;

import java.util.function.Function;

/**
 * Abstraction over "run this unit of work inside a transaction". Kept as an
 * interface (rather than a concrete Hibernate-bound class) so service unit
 * tests can substitute a lightweight test double instead of touching a real
 * SessionFactory/Session/Transaction chain.
 */
public interface TransactionExecutor {
    <T> T executeInTransaction(Function<Session, T> work);
}
