package com.epam.gym.web.dto;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These response/request DTOs are plain data holders built either by Jackson (deserializing a
 * request body, which only ever exercises the setters actually present in that JSON) or by a
 * static from() factory (which only ever exercises the getters). Either path alone leaves the
 * other half of the accessors dead from a coverage standpoint, so this test drives every
 * accessor directly as a round-trip check.
 */
class WebDtoAccessorTest {

    @Test
    void registrationResponse_roundTrips() {
        RegistrationResponse response = new RegistrationResponse();
        response.setUsername("john.doe");
        response.setPassword("Secret123!");
        assertEquals("john.doe", response.getUsername());
        assertEquals("Secret123!", response.getPassword());

        RegistrationResponse viaCtor = new RegistrationResponse("john.doe", "Secret123!");
        assertEquals("john.doe", viaCtor.getUsername());
    }

    @Test
    void registerTraineeRequest_roundTrips() {
        RegisterTraineeRequest request = new RegisterTraineeRequest();
        request.setFirstName("Giorgi");
        request.setLastName("Beridze");
        request.setDateOfBirth(LocalDate.of(2000, 5, 20));
        request.setAddress("Tbilisi");

        assertEquals("Giorgi", request.getFirstName());
        assertEquals("Beridze", request.getLastName());
        assertEquals(LocalDate.of(2000, 5, 20), request.getDateOfBirth());
        assertEquals("Tbilisi", request.getAddress());
    }

    @Test
    void registerTrainerRequest_roundTrips() {
        RegisterTrainerRequest request = new RegisterTrainerRequest();
        request.setFirstName("Nino");
        request.setLastName("Kapanadze");
        request.setSpecialization("Cardio");

        assertEquals("Nino", request.getFirstName());
        assertEquals("Kapanadze", request.getLastName());
        assertEquals("Cardio", request.getSpecialization());
    }

    @Test
    void traineeShortResponse_fromAndRoundTrip() {
        User user = new User("Giorgi", "Beridze", "giorgi.beridze", "Secret123!", true);
        Trainee trainee = new Trainee(LocalDate.of(2000, 5, 20), "Tbilisi", user);

        TraineeShortResponse response = TraineeShortResponse.from(trainee);
        assertEquals("giorgi.beridze", response.getUsername());
        assertEquals("Giorgi", response.getFirstName());
        assertEquals("Beridze", response.getLastName());

        TraineeShortResponse manual = new TraineeShortResponse();
        manual.setUsername("x");
        manual.setFirstName("y");
        manual.setLastName("z");
        assertEquals("x", manual.getUsername());
        assertEquals("y", manual.getFirstName());
        assertEquals("z", manual.getLastName());
    }

    @Test
    void trainerShortResponse_fromAndRoundTrip() {
        TrainingType cardio = new TrainingType("Cardio");
        User user = new User("Nino", "Kapanadze", "nino.kapanadze", "Secret123!", true);
        Trainer trainer = new Trainer(cardio, user);

        TrainerShortResponse response = TrainerShortResponse.from(trainer);
        assertEquals("nino.kapanadze", response.getUsername());
        assertEquals("Cardio", response.getSpecialization());

        TrainerShortResponse manual = new TrainerShortResponse();
        manual.setUsername("x");
        manual.setFirstName("y");
        manual.setLastName("z");
        manual.setSpecialization("w");
        assertEquals("x", manual.getUsername());
        assertEquals("y", manual.getFirstName());
        assertEquals("z", manual.getLastName());
        assertEquals("w", manual.getSpecialization());
    }

    @Test
    void traineeProfileResponse_fromAndRoundTrip() {
        User user = new User("Giorgi", "Beridze", "giorgi.beridze", "Secret123!", true);
        Trainee trainee = new Trainee(LocalDate.of(2000, 5, 20), "Tbilisi", user);

        TraineeProfileResponse response = TraineeProfileResponse.from(trainee);
        assertEquals("giorgi.beridze", response.getUsername());
        assertTrue(response.isActive());
        assertTrue(response.getTrainers().isEmpty());

        TraineeProfileResponse manual = new TraineeProfileResponse();
        manual.setUsername("x");
        manual.setFirstName("y");
        manual.setLastName("z");
        manual.setDateOfBirth(LocalDate.of(1999, 1, 1));
        manual.setAddress("addr");
        manual.setActive(false);
        manual.setTrainers(List.of());
        assertEquals("x", manual.getUsername());
        assertEquals("y", manual.getFirstName());
        assertEquals("z", manual.getLastName());
        assertEquals(LocalDate.of(1999, 1, 1), manual.getDateOfBirth());
        assertEquals("addr", manual.getAddress());
        assertFalse(manual.isActive());
        assertTrue(manual.getTrainers().isEmpty());
    }

    @Test
    void trainerProfileResponse_fromAndRoundTrip() {
        TrainingType cardio = new TrainingType("Cardio");
        User user = new User("Nino", "Kapanadze", "nino.kapanadze", "Secret123!", true);
        Trainer trainer = new Trainer(cardio, user);

        TrainerProfileResponse response = TrainerProfileResponse.from(trainer);
        assertEquals("nino.kapanadze", response.getUsername());
        assertEquals("Cardio", response.getSpecialization());
        assertTrue(response.isActive());
        assertTrue(response.getTrainees().isEmpty());

        TrainerProfileResponse manual = new TrainerProfileResponse();
        manual.setUsername("x");
        manual.setFirstName("y");
        manual.setLastName("z");
        manual.setSpecialization("w");
        manual.setActive(false);
        manual.setTrainees(List.of());
        assertEquals("x", manual.getUsername());
        assertEquals("y", manual.getFirstName());
        assertEquals("z", manual.getLastName());
        assertEquals("w", manual.getSpecialization());
        assertFalse(manual.isActive());
        assertTrue(manual.getTrainees().isEmpty());
    }

    @Test
    void trainerProfileResponse_from_handlesNullSpecialization() {
        User user = new User("Nino", "Kapanadze", "nino.kapanadze", "Secret123!", true);
        Trainer trainer = new Trainer(null, user);

        TrainerProfileResponse response = TrainerProfileResponse.from(trainer);

        assertNull(response.getSpecialization());
    }

    @Test
    void trainingTypeResponse_fromAndRoundTrip() {
        TrainingType cardio = new TrainingType("Cardio");
        cardio.setId(1L);

        TrainingTypeResponse response = TrainingTypeResponse.from(cardio);
        assertEquals("Cardio", response.getTrainingType());
        assertEquals(1L, response.getTrainingTypeId());

        TrainingTypeResponse manual = new TrainingTypeResponse();
        manual.setTrainingType("Yoga");
        manual.setTrainingTypeId(2L);
        assertEquals("Yoga", manual.getTrainingType());
        assertEquals(2L, manual.getTrainingTypeId());
    }

    @Test
    void traineeTrainingResponse_roundTrips() {
        TraineeTrainingResponse response = new TraineeTrainingResponse();
        response.setTrainingName("Morning Cardio");
        response.setTrainingDate(LocalDate.now());
        response.setTrainingType("Cardio");
        response.setTrainingDuration(45);
        response.setTrainerName("Nino Kapanadze");

        assertEquals("Morning Cardio", response.getTrainingName());
        assertEquals("Cardio", response.getTrainingType());
        assertEquals(45, response.getTrainingDuration());
        assertEquals("Nino Kapanadze", response.getTrainerName());
    }

    @Test
    void trainerTrainingResponse_roundTrips() {
        TrainerTrainingResponse response = new TrainerTrainingResponse();
        response.setTrainingName("Morning Cardio");
        response.setTrainingDate(LocalDate.now());
        response.setTrainingType("Cardio");
        response.setTrainingDuration(45);
        response.setTraineeName("Giorgi Beridze");

        assertEquals("Morning Cardio", response.getTrainingName());
        assertEquals("Cardio", response.getTrainingType());
        assertEquals(45, response.getTrainingDuration());
        assertEquals("Giorgi Beridze", response.getTraineeName());
    }

    @Test
    void updateTraineeProfileRequest_roundTrips() {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest();
        request.setFirstName("Giorgi");
        request.setLastName("Beridze");
        request.setDateOfBirth(LocalDate.of(2000, 5, 20));
        request.setAddress("Tbilisi");
        request.setIsActive(true);

        assertEquals("Giorgi", request.getFirstName());
        assertEquals("Beridze", request.getLastName());
        assertEquals(LocalDate.of(2000, 5, 20), request.getDateOfBirth());
        assertEquals("Tbilisi", request.getAddress());
        assertTrue(request.getIsActive());
    }
}
