package com.epam.gym.health;

import com.epam.gym.entity.TrainingType;
import com.epam.gym.service.TrainingTypeService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Business-level health signal: the TrainingType table is a constant reference list the application
 * can never write to at runtime (task note #14), so if it's empty — misconfigured environment,
 * seeding never ran, wrong database — every trainer registration and training creation will fail.
 * Surfacing that as DOWN catches the problem at the health check instead of on the first real request.
 */
@Component("trainingTypes")
public class TrainingTypesHealthIndicator implements HealthIndicator {

    private final TrainingTypeService trainingTypeService;

    public TrainingTypesHealthIndicator(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @Override
    public Health health() {
        try {
            List<TrainingType> types = trainingTypeService.getAll();
            if (types.isEmpty()) {
                return Health.down().withDetail("trainingTypes", "reference list is empty").build();
            }
            return Health.up().withDetail("count", types.size()).build();
        } catch (RuntimeException e) {
            return Health.down(e).build();
        }
    }
}
