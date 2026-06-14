package com.pms.authservice.service.adapter;

public interface RegistrationEventPublisher {
    void publish(Object event);
}
