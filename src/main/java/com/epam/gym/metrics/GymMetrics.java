package com.epam.gym.metrics;

import com.epam.gym.service.TrainingTypeService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom application metrics, exposed to Prometheus via {@code /actuator/prometheus} alongside the
 * standard JVM/HTTP metrics Micrometer registers automatically. Each business event of interest
 * (registration, login, training creation) increments a counter at the point it happens in the
 * controller layer; the training-type count is a gauge since it reads current state on scrape rather
 * than counting an event.
 */
@Component
public class GymMetrics {

    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter trainingsAdded;
    private final Counter loginSuccesses;
    private final Counter loginFailures;

    public GymMetrics(MeterRegistry registry, TrainingTypeService trainingTypeService) {
        this.traineeRegistrations = Counter.builder("gym_trainee_registrations_total")
                .description("Total number of trainee profiles created")
                .register(registry);
        this.trainerRegistrations = Counter.builder("gym_trainer_registrations_total")
                .description("Total number of trainer profiles created")
                .register(registry);
        this.trainingsAdded = Counter.builder("gym_trainings_added_total")
                .description("Total number of trainings added")
                .register(registry);
        this.loginSuccesses = Counter.builder("gym_login_attempts_total")
                .description("Total number of login attempts, by outcome")
                .tag("result", "success")
                .register(registry);
        this.loginFailures = Counter.builder("gym_login_attempts_total")
                .description("Total number of login attempts, by outcome")
                .tag("result", "failure")
                .register(registry);
        Gauge.builder("gym_training_types", trainingTypeService, s -> s.getAll().size())
                .description("Current size of the constant training-type reference list")
                .register(registry);
    }

    public void incrementTraineeRegistrations() {
        traineeRegistrations.increment();
    }

    public void incrementTrainerRegistrations() {
        trainerRegistrations.increment();
    }

    public void incrementTrainingsAdded() {
        trainingsAdded.increment();
    }

    public void incrementLoginSuccess() {
        loginSuccesses.increment();
    }

    public void incrementLoginFailure() {
        loginFailures.increment();
    }
}
