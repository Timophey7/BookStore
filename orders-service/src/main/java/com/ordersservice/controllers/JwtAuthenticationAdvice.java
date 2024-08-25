package com.ordersservice.controllers;

import com.ordersservice.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;

@ControllerAdvice
@RequiredArgsConstructor
@Component
public class JwtAuthenticationAdvice {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationAdvice.class);
    private final JwtService jwtService;

    @ModelAttribute
    public void setUserInSession(@RequestHeader(value = "Authorization", required = false) String token, HttpServletRequest request) {
        HttpSession session= request.getSession();
        if (session != null && token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            log.info("jwtToken : "+token);
            String email = jwtService.extractUsername(jwtToken);
            log.info("Email in controller advice :" +email);
            if (email != null) {
                session.setAttribute("email", email);
                log.info("add email in session");
            }
        }
    }
}
