package com.pms.apigateway.routes;

import com.pms.apigateway.filter.JwtValidationGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Routes {

    public Routes(JwtValidationGatewayFilterFactory jwtValidationGatewayFilterFactory) {
        this.jwtValidationGatewayFilterFactory = jwtValidationGatewayFilterFactory;
    }

    private final JwtValidationGatewayFilterFactory jwtValidationGatewayFilterFactory;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("patient-service-route", r -> r
                        .path("/api/patients/**")
                        .filters(f -> f.stripPrefix(1).filter(jwtValidationGatewayFilterFactory.apply(new Object())))
                        .uri("http://patient-service:4000"))
                .route("api-docs-patient", r -> r
                        .path("/api-docs/patients")
                        .filters(f -> f.rewritePath("/api-docs/patients", "/v3/api-docs"))
                        .uri("http://patient-service:4000"))
                .route("auth-service-route", r -> r
                        .path("/auth/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://auth-service:4005"))
                .route("api-docs-auth-route", r -> r
                        .path("/api-docs/auth")
                        .filters(f -> f.rewritePath("/api-docs/auth", "/v3/api-docs"))
                        .uri("http://auth-service:4005"))
                .build();
    }
}
