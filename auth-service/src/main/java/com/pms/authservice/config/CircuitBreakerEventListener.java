package com.pms.authservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnCallNotPermittedEvent;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnErrorEvent;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CircuitBreakerEventListener {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerEventListener.class);

    private final MeterRegistry meterRegistry;

    public CircuitBreakerEventListener(CircuitBreakerRegistry registry, MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        registry.getAllCircuitBreakers().forEach(cb -> {
            cb.getEventPublisher()
                    .onStateTransition(event -> handleTransition(event))
                    .onError(event -> handleError(event))
                    .onCallNotPermitted(event -> handleCallNotPermitted(event));
        });
    }

    private void handleCallNotPermitted(CircuitBreakerOnCallNotPermittedEvent event) {
        meterRegistry.counter("circuitbreaker.call.rejected",
                "name", event.getCircuitBreakerName()
        ).increment();
    }

    private void handleError(CircuitBreakerOnErrorEvent event) {
        meterRegistry.counter("circuitbreaker.call.failed",
                "name", event.getCircuitBreakerName()
        ).increment();
    }

    private void handleTransition(CircuitBreakerOnStateTransitionEvent event) {
        String name = event.getCircuitBreakerName();
        CircuitBreaker.State from = event.getStateTransition().getFromState();
        CircuitBreaker.State to = event.getStateTransition().getToState();

        log.warn("Circuit breaker transition from {} to {}", from, to);

        meterRegistry.counter("circuitbreaker.state.transition",
                "name", name,
                "from", from.name(),
                "to", to.name()
        ).increment();

        if(to == CircuitBreaker.State.OPEN){
            log.error("ALERT: circuit breaker {} is now open! Downstream Service may be down", name);
        }
    }
}
