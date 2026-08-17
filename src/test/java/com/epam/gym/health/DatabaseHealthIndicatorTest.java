package com.epam.gym.health;

import com.epam.gym.util.TransactionExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private TransactionExecutor transactionExecutor;

    @Test
    void health_reportsUp_whenQuerySucceeds() {
        when(transactionExecutor.executeInTransaction(any())).thenReturn(1);

        Health health = new DatabaseHealthIndicator(transactionExecutor).health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("reachable", health.getDetails().get("database"));
    }

    @Test
    void health_reportsDown_whenQueryThrows() {
        when(transactionExecutor.executeInTransaction(any())).thenThrow(new RuntimeException("connection refused"));

        Health health = new DatabaseHealthIndicator(transactionExecutor).health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("unreachable", health.getDetails().get("database"));
    }
}
