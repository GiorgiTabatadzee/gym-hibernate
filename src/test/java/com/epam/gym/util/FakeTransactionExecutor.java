package com.epam.gym.util;

import org.hibernate.Session;

import java.util.function.Function;

import static org.mockito.Mockito.mock;

/**
 * Test double for TransactionExecutor: runs the unit of work immediately
 * against a Mockito mock Session, with no real Hibernate transaction. Lets
 * service unit tests mock the DAO layer (which takes a Session argument)
 * without standing up a real SessionFactory.
 */
public class FakeTransactionExecutor implements TransactionExecutor {
    @Override
    public <T> T executeInTransaction(Function<Session, T> work) {
        return work.apply(mock(Session.class));
    }
}
