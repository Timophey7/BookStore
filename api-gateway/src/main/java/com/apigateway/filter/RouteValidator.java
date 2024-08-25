package com.apigateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> apiEndpoints = List.of(
            "/v1/auth/register",
            "/v1/auth/authenticate",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecured =
            serverHttpRequest -> apiEndpoints.stream().noneMatch(uri -> serverHttpRequest.getURI().getPath().contains(uri));

}
