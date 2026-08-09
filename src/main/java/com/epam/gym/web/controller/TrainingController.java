package com.epam.gym.web.controller;

import com.epam.gym.dto.TrainingCreateRequest;
import com.epam.gym.service.TrainingService;
import com.epam.gym.web.dto.AddTrainingRequest;
import com.epam.gym.web.security.AuthCredentials;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainings")
@Api(tags = "Training")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping
    @ApiOperation(value = "Add a training", notes = "Requires HTTP Basic authentication as the assigned trainer.")
    @ApiImplicitParams(@ApiImplicitParam(name = "Authorization", value = "Basic auth credentials (trainer)",
            required = true, paramType = "header", dataTypeClass = String.class))
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request,
                                             AuthCredentials credentials) {
        TrainingCreateRequest serviceRequest = new TrainingCreateRequest();
        serviceRequest.setTraineeUsername(request.getTraineeUsername());
        serviceRequest.setTrainerUsername(request.getTrainerUsername());
        serviceRequest.setTrainingName(request.getTrainingName());
        serviceRequest.setTrainingDate(request.getTrainingDate());
        serviceRequest.setTrainingDurationMinutes(request.getTrainingDuration());
        trainingService.addTraining(serviceRequest, credentials.getPassword());
        return ResponseEntity.ok().build();
    }
}
