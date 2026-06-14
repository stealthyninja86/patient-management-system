package com.pms.authservice.service.strategy;

import com.pms.authservice.dto.event.DoctorRegisteredEvent;
import com.pms.authservice.dto.request.DoctorRegisterRequestDTO;
import com.pms.authservice.dto.response.DoctorRegisterResponseDTO;
import com.pms.authservice.grpc.HospitalGrpcClient;
import com.pms.authservice.model.Role;
import com.pms.authservice.model.User;
import com.pms.authservice.service.UserService;
import com.pms.authservice.service.adapter.RegistrationEventPublisher;
import com.pms.authservice.service.mapper.UserMapper;
import com.pms.authservice.service.saga.RegistrationSaga;
import hospital.DoctorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DoctorRegistrationStrategy implements RegistrationStrategy<DoctorRegisterRequestDTO, DoctorRegisterResponseDTO> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final HospitalGrpcClient hospitalGrpcClient;
    private final UserService userService;
    private final UserMapper userMapper;
    private final RegistrationEventPublisher eventPublisher;
    private final ObjectProvider<RegistrationSaga> sagaProvider;

    public DoctorRegistrationStrategy(HospitalGrpcClient hospitalGrpcClient,
                                      UserService userService, UserMapper userMapper,
                                      RegistrationEventPublisher eventPublisher,
                                      ObjectProvider<RegistrationSaga> sagaProvider) {
        this.hospitalGrpcClient = hospitalGrpcClient;
        this.userService = userService;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
        this.sagaProvider = sagaProvider;
    }

    @Override
    public DoctorRegisterResponseDTO register(DoctorRegisterRequestDTO request, String encodedPassword) {
        RegistrationSaga saga = sagaProvider.getObject();
        try {
            DoctorResponse doctorResponse = hospitalGrpcClient.createDoctor(
                    request.name(), request.email(), request.phone(), request.departmentId(), request.hospitalId());

            saga.addCompensation(() -> {
                try {
                    hospitalGrpcClient.deleteDoctor(doctorResponse.getDoctorId());
                } catch (Exception e) {
                    log.error("failed to delete doctor", e);
                }
            });

            User user = userMapper.toEntity(request, encodedPassword, doctorResponse.getDoctorId());
            saga.addCompensation(() -> userService.deleteUser(user));
            userService.saveUser(user);
            eventPublisher.publish(new DoctorRegisteredEvent(
                    request.email(), doctorResponse.getDoctorId(), request.hospitalId(), Instant.now()));
            saga.complete();
            return userMapper.toDoctorResponse(user, doctorResponse.getDoctorId());
        } catch (Exception e) {
            saga.compensate();
            throw e;
        }
    }

    @Override
    public Role supportedRole() {
        return Role.DOCTOR;
    }
}