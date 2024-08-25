package com.ordersservice.securityjwtservice.controller;


import com.ordersservice.securityjwtservice.models.AuthenticateRequest;
import com.ordersservice.securityjwtservice.models.AuthenticationResponse;
import com.ordersservice.securityjwtservice.models.RegisterRequest;
import com.ordersservice.securityjwtservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest registerRequest
    ){
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticateRequest authenticateRequest
    ){
        return ResponseEntity.ok(authenticationService.authenticate(authenticateRequest));
    }

}
