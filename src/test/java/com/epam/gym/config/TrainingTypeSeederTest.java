package com.epam.gym.config;

import com.epam.gym.entity.TrainingType;
import com.epam.gym.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeSeederTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @Test
    void run_seedsDefaultTypes_whenTableEmpty() {
        when(trainingTypeService.getAll()).thenReturn(List.of());

        new TrainingTypeSeeder(trainingTypeService).run();

        verify(trainingTypeService, times(6)).create(anyString());
    }

    @Test
    void run_doesNothing_whenTableAlreadySeeded() {
        TrainingType cardio = new TrainingType("Cardio");
        when(trainingTypeService.getAll()).thenReturn(List.of(cardio));

        new TrainingTypeSeeder(trainingTypeService).run();

        verify(trainingTypeService, never()).create(anyString());
    }
}
