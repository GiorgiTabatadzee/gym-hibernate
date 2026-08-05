package com.epam.gym.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainees")
public class Trainee extends BaseEntity {

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    // 1:1 parent-child with User (task note #4). EAGER is intentional here:
    // a plain unidirectional @OneToOne can't be made truly lazy without
    // bytecode enhancement, and User is needed on virtually every access
    // path (credentials, name), so marking it LAZY would be misleading.
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Trainee <-> Trainer is a many-to-many relation (task note #5), independent
    // of Training records. Trainee is the owning side of the join table.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private Set<Trainer> trainers = new HashSet<>();

    // Hard-deleting a trainee must cascade-delete their trainings (task note #7).
    @OneToMany(mappedBy = "trainee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Training> trainings = new HashSet<>();

    public Trainee() {
    }

    public Trainee(LocalDate dateOfBirth, String address, User user) {
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.user = user;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Trainer> getTrainers() {
        return trainers;
    }

    public void setTrainers(Set<Trainer> trainers) {
        this.trainers = trainers;
    }

    public Set<Training> getTrainings() {
        return trainings;
    }

    @Override
    public String toString() {
        return "Trainee{id=" + getId() + ", user=" + (user != null ? user.getUsername() : null) + "}";
    }
}
