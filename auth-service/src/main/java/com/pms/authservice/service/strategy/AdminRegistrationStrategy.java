package com.pms.authservice.service.strategy;

import com.pms.authservice.dto.event.AdminRegisteredEvent;
import com.pms.authservice.dto.request.AdminRegisterRequestDTO;
import com.pms.authservice.dto.response.AdminRegisterResponseDTO;
import com.pms.authservice.model.Role;
import com.pms.authservice.model.User;
import com.pms.authservice.service.UserService;
import com.pms.authservice.service.adapter.RegistrationEventPublisher;
import com.pms.authservice.service.mapper.UserMapper;
import com.pms.authservice.service.saga.RegistrationSaga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AdminRegistrationStrategy implements RegistrationStrategy<AdminRegisterRequestDTO, AdminRegisterResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(AdminRegistrationStrategy.class);
    private final UserService userService;
    private final UserMapper userMapper;
    private final RegistrationEventPublisher eventPublisher;
    private final ObjectProvider<RegistrationSaga> sagaProvider;

    public AdminRegistrationStrategy(UserService userService, UserMapper userMapper,
                                     RegistrationEventPublisher eventPublisher,
                                     ObjectProvider<RegistrationSaga> sagaProvider) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
        this.sagaProvider = sagaProvider;
    }

    @Override
    public AdminRegisterResponseDTO register(AdminRegisterRequestDTO request, String encodedPassword) {
        RegistrationSaga saga = sagaProvider.getObject();
        try {
            User user = userMapper.toEntity(request, encodedPassword);
            saga.addCompensation(() -> userService.deleteUser(user));
            userService.saveUser(user);
            eventPublisher.publish(new AdminRegisteredEvent(request.email(), Instant.now()));
            saga.complete();
            return userMapper.toAdminResponse(user);
        }
        catch (Exception e) {
            saga.compensate();
            throw e;
        }
    }

    @Override
    public Role supportedRole() {
        return Role.ADMIN;
    }
}