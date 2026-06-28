package com.pms.authservice.service.strategy;

public interface RegistrationEventPublisherStrategy {
    void publish(Object event);
}
