package com.ordersservice.securityjwtservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordersservice.securityjwtservice.JwtAuthFilter;
import com.ordersservice.securityjwtservice.JwtService;
import com.ordersservice.securityjwtservice.models.AuthenticateRequest;
import com.ordersservice.securityjwtservice.models.AuthenticationResponse;
import com.ordersservice.securityjwtservice.models.RegisterRequest;
import com.ordersservice.securityjwtservice.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthenticationService authenticationService;

    @MockBean
    JwtAuthFilter jwtAuthFilter;

    @MockBean
    JwtService jwtService;

    AuthenticateRequest authenticateRequest;
    RegisterRequest registerRequest;
    AuthenticationResponse authenticationResponse;
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("email@email.com");
        registerRequest.setPassword("password");
        authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setToken("tokenTest");
        authenticateRequest = new AuthenticateRequest();
        authenticateRequest.setEmail("email@email.com");
        authenticateRequest.setPassword("password");
    }

    @Test
    void register() throws Exception {
        when(authenticationService.register(registerRequest)).thenReturn(authenticationResponse);

        ResultActions response = mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        );

        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(authenticationResponse.getToken()));

    }

    @Test
    void authenticate() throws Exception {

        when(authenticationService.authenticate(authenticateRequest)).thenReturn(authenticationResponse);

        ResultActions response = mockMvc.perform(post("/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticateRequest))
        );

        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(authenticationResponse.getToken()));




    }
}