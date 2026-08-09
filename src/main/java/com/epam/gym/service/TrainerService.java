package com.epam.gym.service;

import com.epam.gym.dto.TrainerTrainingCriteria;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;

import java.util.List;

public interface TrainerService {

    /** #1 Create Trainer profile. Username/password are generated, not supplied. */
    Trainer createTrainerProfile(String firstName, String lastName, String specializationName);

    /** #4 Trainer username and password matching. */
    boolean matchCredentials(String username, String password);

    /** #5 Select Trainer profile by username (requires authentication). */
    Trainer getProfileByUsername(String username, String password);

    /** #8 Trainer password change (requires authentication with the OLD password). */
    void changePassword(String username, String oldPassword, String newPassword);

    /**
     * #9 Update trainer profile (requires authentication). Unlike {@link #setActive}, setting
     * isActive here is a plain idempotent field update — it's part of a full profile replace, not
     * the dedicated activate/de-activate action. Specialization is read-only and not updatable here.
     */
    Trainer updateProfile(String username, String password, String firstName, String lastName, boolean isActive);

    /** #12 Activate/De-activate trainer. Not idempotent: throws if already in the requested state. */
    void setActive(String username, String password, boolean active);

    /** #15 Get Trainer Trainings List by username and optional criteria. */
    List<Training> getTrainingsList(String username, String password, TrainerTrainingCriteria criteria);
}
