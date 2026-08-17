package com.epam.gym.health;

import com.epam.gym.util.TransactionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Reports whether the configured database is actually reachable, by running a trivial query
 * through the same {@link TransactionExecutor} every DAO call goes through (so it exercises the
 * real connection pool / SessionFactory, not a separate ad-hoc check).
 */
@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private final TransactionExecutor transactionExecutor;

    public DatabaseHealthIndicator(TransactionExecutor transactionExecutor) {
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    public Health health() {
        long start = System.currentTimeMillis();
        try {
            transactionExecutor.executeInTransaction(
                    session -> session.createNativeQuery("SELECT 1", Integer.class).getSingleResult());
            long elapsedMs = System.currentTimeMillis() - start;
            return Health.up()
                    .withDetail("database", "reachable")
                    .withDetail("responseTimeMs", elapsedMs)
                    .build();
        } catch (RuntimeException e) {
            log.warn("Database health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("database", "unreachable")
                    .withException(e)
                    .build();
        }
    }
}
