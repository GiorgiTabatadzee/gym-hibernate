package com.epam.gym.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Constant reference list (e.g. "Cardio", "Yoga", "Strength"). Rows are seeded
 * once and never updated from the application layer (see task note #12) —
 * there is deliberately no update/delete method in the service for this entity.
 */
@Entity
@Table(name = "training_types", uniqueConstraints = @UniqueConstraint(columnNames = "training_type_name"))
public class TrainingType extends BaseEntity {

    @Column(name = "training_type_name", nullable = false, unique = true)
    private String trainingTypeName;

    public TrainingType() {
    }

    public TrainingType(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    public String getTrainingTypeName() {
        return trainingTypeName;
    }

    public void setTrainingTypeName(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    @Override
    public String toString() {
        return "TrainingType{id=" + getId() + ", name='" + trainingTypeName + "'}";
    }
}
