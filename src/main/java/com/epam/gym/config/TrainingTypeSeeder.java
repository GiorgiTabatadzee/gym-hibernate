package com.epam.gym.config;

import com.epam.gym.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the constant Training Type reference list on startup, if empty. Task note #14: this table
 * "could not be updated from the application" — there is no REST endpoint to create one, so
 * something has to put the initial constant values in.
 */
@Component
public class TrainingTypeSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeSeeder.class);

    private static final List<String> DEFAULT_TRAINING_TYPES =
            List.of("Cardio", "Strength", "Yoga", "CrossFit", "Stretching", "Zumba");

    private final TrainingTypeService trainingTypeService;

    public TrainingTypeSeeder(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @Override
    public void run(String... args) {
        if (!trainingTypeService.getAll().isEmpty()) {
            return;
        }
        DEFAULT_TRAINING_TYPES.forEach(trainingTypeService::create);
        log.info("Seeded {} default training types", DEFAULT_TRAINING_TYPES.size());
    }
}
