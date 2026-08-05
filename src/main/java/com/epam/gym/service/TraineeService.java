package com.epam.gym.service;

import com.epam.gym.dto.TraineeTrainingCriteria;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;

import java.time.LocalDate;
import java.util.List;

public interface TraineeService {

    /** #2 Create Trainee profile. Username/password are generated, not supplied. */
    Trainee createTraineeProfile(String firstName, String lastName, LocalDate dateOfBirth, String address);

    /** #3 Trainee username and password matching. */
    boolean matchCredentials(String username, String password);

    /** #6 Select Trainee profile by username (requires authentication). */
    Trainee getProfileByUsername(String username, String password);

    /** #7 Trainee password change (requires authentication with the OLD password). */
    void changePassword(String username, String oldPassword, String newPassword);

    /** #10 Update trainee profile (requires authentication). */
    Trainee updateProfile(String username, String password, String firstName, String lastName,
                           LocalDate dateOfBirth, String address);

    /** #11 Activate/De-activate trainee. Not idempotent: throws if already in the requested state. */
    void setActive(String username, String password, boolean active);

    /** #13 Delete trainee profile by username (hard delete, cascades trainings). */
    void deleteProfileByUsername(String username, String password);

    /** #14 Get Trainee Trainings List by username and optional criteria. */
    List<Training> getTrainingsList(String username, String password, TraineeTrainingCriteria criteria);

    /** #17 Get trainers list not yet assigned to this trainee. */
    List<Trainer> getTrainersNotAssigned(String username, String password);

    /** #18 Update trainee's trainers list (full replace). */
    void updateTrainersList(String username, String password, List<String> trainerUsernames);
}
