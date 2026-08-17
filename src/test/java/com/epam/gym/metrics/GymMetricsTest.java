package com.epam.gym.metrics;

import com.epam.gym.entity.TrainingType;
import com.epam.gym.service.TrainingTypeService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymMetricsTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    private SimpleMeterRegistry registry;
    private GymMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new GymMetrics(registry, trainingTypeService);
    }

    @Test
    void incrementTraineeRegistrations_incrementsCounter() {
        metrics.incrementTraineeRegistrations();
        metrics.incrementTraineeRegistrations();

        assertEquals(2.0, registry.get("gym_trainee_registrations_total").counter().count());
    }

    @Test
    void incrementTrainerRegistrations_incrementsCounter() {
        metrics.incrementTrainerRegistrations();

        assertEquals(1.0, registry.get("gym_trainer_registrations_total").counter().count());
    }

    @Test
    void incrementTrainingsAdded_incrementsCounter() {
        metrics.incrementTrainingsAdded();

        assertEquals(1.0, registry.get("gym_trainings_added_total").counter().count());
    }

    @Test
    void loginCounters_areTaggedByResult() {
        metrics.incrementLoginSuccess();
        metrics.incrementLoginSuccess();
        metrics.incrementLoginFailure();

        assertEquals(2.0, registry.get("gym_login_attempts_total").tag("result", "success").counter().count());
        assertEquals(1.0, registry.get("gym_login_attempts_total").tag("result", "failure").counter().count());
    }

    @Test
    void trainingTypesGauge_reflectsCurrentServiceState() {
        when(trainingTypeService.getAll()).thenReturn(List.of(new TrainingType("Cardio"), new TrainingType("Yoga")));

        double value = registry.get("gym_training_types").gauge().value();

        assertEquals(2.0, value);
    }
}
