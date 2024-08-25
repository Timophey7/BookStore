package com.ordersservice.securityjwtservice.service;

import com.ordersservice.securityjwtservice.JwtService;
import com.ordersservice.securityjwtservice.models.*;
import com.ordersservice.securityjwtservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setFirstname("test");
        user.setLastname("test");
    }

    @Test
    void register() {
        RegisterRequest registerRequest = new RegisterRequest(
                "test",
                "testL",
                "test@test.com",
                "password123"
        );
        User user = User.builder()
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .email(registerRequest.getEmail())
                .password("encodedPassword")
                .role(Role.USER)
                .build();
        String token = "testToken";
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(user)).thenReturn(token);

        AuthenticationResponse response = authenticationService.register(registerRequest);

        verify(userRepository).save(user);
        verify(jwtService).generateToken(user);
        assertEquals(token, response.getToken());


    }

    @Test
    void authenticate() {
        AuthenticateRequest authenticateRequest = new AuthenticateRequest("test@test.com", "password");
        String token = "testToken";
        when(userRepository.findByEmail(authenticateRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(token);

        Authentication authentication = new UsernamePasswordAuthenticationToken(authenticateRequest.getEmail(), authenticateRequest.getPassword());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);


        AuthenticationResponse response = authenticationService.authenticate(authenticateRequest);


        verify(authenticationManager).authenticate(authentication);
        verify(userRepository).findByEmail(authenticateRequest.getEmail());
        verify(jwtService).generateToken(user);
        assertEquals(token, response.getToken());

    }


}