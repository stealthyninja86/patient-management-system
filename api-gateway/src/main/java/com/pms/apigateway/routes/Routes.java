package com.pms.apigateway.routes;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Routes {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("patient-service-route", r -> r
                        .path("/api/patients/**")
                        .filters(f -> f.stripPrefix(1))
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
                .route("hospital-service-route", r -> r
                        .path("/api/hospitals/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://hospital-service:4003"))
                .route("doctor-service-route", r -> r
                        .path("/api/doctors/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://hospital-service:4003"))
                .route("department-service-route", r -> r
                        .path("/api/departments/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://hospital-service:4003"))
                .route("api-docs-hospital", r -> r
                        .path("/api-docs/hospitals")
                        .filters(f -> f.rewritePath("/api-docs/hospitals", "/v3/api-docs"))
                        .uri("http://hospital-service:4003"))
                .route("clinical-service-route", r -> r
                        .path("/api/prescriptions/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://clinical-service:4010"))
                .route("api-docs-clinical", r -> r
                        .path("/api-docs/clinical")
                        .filters(f -> f.rewritePath("/api-docs/clinical", "/v3/api-docs"))
                        .uri("http://clinical-service:4010"))
                .route("schedule-service-route", r -> r
                        .path("/api/appointments/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://schedule-service:4009"))
                .route("timeslot-service-route", r -> r
                        .path("/api/time-slots/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://schedule-service:4009"))
                .route("api-docs-schedule", r -> r
                        .path("/api-docs/schedule")
                        .filters(f -> f.rewritePath("/api-docs/schedule", "/v3/api-docs"))
                        .uri("http://schedule-service:4009"))
                .route("notification-service-route", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://notification-service:4014"))
                .route("consent-route", r -> r
                        .path("/api/consent/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://notification-service:4014"))
                .route("api-docs-notifications", r -> r
                        .path("/api-docs/notifications")
                        .filters(f -> f.rewritePath("/api-docs/notifications", "/v3/api-docs"))
                        .uri("http://notification-service:4014"))
                .build();
    }
}
