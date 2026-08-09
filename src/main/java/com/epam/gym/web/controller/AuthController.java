package com.epam.gym.web.controller;

import com.epam.gym.service.AuthenticationService;
import com.epam.gym.web.dto.ChangeLoginRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
@Api(tags = "Authentication")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/login")
    @ApiOperation(value = "Login", notes = "Public endpoint — this call itself performs the authentication "
            + "check for the given username/password pair.")
    public ResponseEntity<Void> login(@ApiParam(required = true) @RequestParam @NotBlank String username,
                                       @ApiParam(required = true) @RequestParam @NotBlank String password) {
        authenticationService.authenticate(username, password);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @ApiOperation(value = "Change login password", notes = "Public endpoint — authenticates via the supplied "
            + "old password, which acts as the credential check for this call.")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangeLoginRequest request) {
        authenticationService.changePassword(
                request.getUsername(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
