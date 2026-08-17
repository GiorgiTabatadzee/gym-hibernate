package com.epam.gym.health;

import com.epam.gym.entity.TrainingType;
import com.epam.gym.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypesHealthIndicatorTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @Test
    void health_reportsUp_whenReferenceListNotEmpty() {
        when(trainingTypeService.getAll()).thenReturn(List.of(new TrainingType("Cardio")));

        Health health = new TrainingTypesHealthIndicator(trainingTypeService).health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(1, health.getDetails().get("count"));
    }

    @Test
    void health_reportsDown_whenReferenceListEmpty() {
        when(trainingTypeService.getAll()).thenReturn(List.of());

        Health health = new TrainingTypesHealthIndicator(trainingTypeService).health();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void health_reportsDown_whenServiceThrows() {
        when(trainingTypeService.getAll()).thenThrow(new RuntimeException("db down"));

        Health health = new TrainingTypesHealthIndicator(trainingTypeService).health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}
