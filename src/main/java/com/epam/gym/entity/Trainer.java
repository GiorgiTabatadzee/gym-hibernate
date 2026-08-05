package com.epam.gym.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainers")
public class Trainer extends BaseEntity {

    // Specialization is a FK to the constant TrainingType list (per schema).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;

    // 1:1 parent-child with User (task note #4). EAGER is intentional here —
    // see the same note on Trainee.user.
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Inverse side of the Trainee <-> Trainer many-to-many relation.
    @ManyToMany(mappedBy = "trainers", fetch = FetchType.LAZY)
    private Set<Trainee> trainees = new HashSet<>();

    // Trainer deletion is out of scope for this task, so no cascade here.
    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    private Set<Training> trainings = new HashSet<>();

    public Trainer() {
    }

    public Trainer(TrainingType specialization, User user) {
        this.specialization = specialization;
        this.user = user;
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Trainee> getTrainees() {
        return trainees;
    }

    public Set<Training> getTrainings() {
        return trainings;
    }

    @Override
    public String toString() {
        return "Trainer{id=" + getId() + ", user=" + (user != null ? user.getUsername() : null) + "}";
    }
}
