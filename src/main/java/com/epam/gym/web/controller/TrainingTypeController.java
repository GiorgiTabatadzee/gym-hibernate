package com.epam.gym.web.controller;

import com.epam.gym.service.AuthenticationService;
import com.epam.gym.service.TrainingTypeService;
import com.epam.gym.web.dto.TrainingTypeResponse;
import com.epam.gym.web.security.AuthCredentials;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@Api(tags = "Training Type")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;
    private final AuthenticationService authenticationService;

    public TrainingTypeController(TrainingTypeService trainingTypeService,
                                   AuthenticationService authenticationService) {
        this.trainingTypeService = trainingTypeService;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    @ApiOperation(value = "Get the constant list of training types",
            notes = "Requires HTTP Basic authentication as any registered trainee or trainer.")
    @ApiImplicitParams(@ApiImplicitParam(name = "Authorization", value = "Basic auth credentials",
            required = true, paramType = "header", dataTypeClass = String.class))
    public List<TrainingTypeResponse> getTrainingTypes(AuthCredentials credentials) {
        authenticationService.authenticate(credentials.getUsername(), credentials.getPassword());
        return trainingTypeService.getAll().stream().map(TrainingTypeResponse::from).toList();
    }
}
