package com.epam.gym.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BaseEntity.equals()/hashCode() and each entity's toString() aren't exercised by the
 * service/controller tests (which only ever compare by reference or serialize via Jackson, not
 * via equals() or Object#toString()), so they're covered directly here.
 */
class EntityTest {

    @Test
    void equals_sameInstance_isTrue() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        assertTrue(trainee.equals(trainee));
    }

    @Test
    void equals_null_isFalse() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        assertFalse(trainee.equals(null));
    }

    @Test
    void equals_differentClass_isFalse() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        assertNotEquals(trainee, "not-an-entity");
    }

    @Test
    void equals_sameIdSameClass_isTrue() {
        Trainee a = new Trainee();
        a.setId(1L);
        Trainee b = new Trainee();
        b.setId(1L);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentId_isFalse() {
        Trainee a = new Trainee();
        a.setId(1L);
        Trainee b = new Trainee();
        b.setId(2L);
        assertNotEquals(a, b);
    }

    @Test
    void equals_nullId_isFalse() {
        Trainee a = new Trainee();
        Trainee b = new Trainee();
        assertNotEquals(a, b);
    }

    @Test
    void toString_doesNotThrow_andOmitsPassword() {
        User user = new User("Giorgi", "Beridze", "giorgi.beridze", "Secret123!", true);
        user.setId(1L);
        String s = user.toString();
        assertTrue(s.contains("giorgi.beridze"));
        assertFalse(s.contains("Secret123!"));

        Trainee trainee = new Trainee(LocalDate.of(2000, 1, 1), "Tbilisi", user);
        trainee.setId(2L);
        assertTrue(trainee.toString().contains("giorgi.beridze"));

        TrainingType cardio = new TrainingType("Cardio");
        cardio.setId(3L);
        assertTrue(cardio.toString().contains("Cardio"));

        Trainer trainer = new Trainer(cardio, user);
        trainer.setId(4L);
        assertTrue(trainer.toString().contains("giorgi.beridze"));

        Training training = new Training(trainee, trainer, "Morning Cardio", cardio, LocalDate.now(), 45);
        training.setId(5L);
        assertTrue(training.toString().contains("Morning Cardio"));
    }

    @Test
    void trainee_toString_handlesNullUser() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        assertTrue(trainee.toString().contains("null"));
    }

    @Test
    void trainer_toString_handlesNullUser() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        assertTrue(trainer.toString().contains("null"));
    }
}
