package com.pms.clinicalservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import schedule.HasOngoingAppointmentRequest;
import schedule.HasOngoingAppointmentResponse;
import schedule.ScheduleServiceGrpc;

import java.time.LocalDateTime;

@Component
public class ScheduleGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(ScheduleGrpcClient.class);

    private final ScheduleServiceGrpc.ScheduleServiceBlockingStub blockingStub;

    public ScheduleGrpcClient(@Value("${schedule.service.address}") String host,
                              @Value("${schedule.service.grpc.port}") int port) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = ScheduleServiceGrpc.newBlockingStub(channel);
    }

    public OngoingAppointmentResult checkOngoingAppointment(String doctorId, String patientId) {
        log.debug("Checking ongoing appointment for doctor: {}, patient: {}", doctorId, patientId);
        HasOngoingAppointmentRequest request = HasOngoingAppointmentRequest.newBuilder()
                .setDoctorId(doctorId)
                .setPatientId(patientId)
                .build();

        HasOngoingAppointmentResponse response = blockingStub.hasOngoingAppointment(request);
        if (response.getHasOngoing() && !response.getStartTime().isEmpty()) {
            return new OngoingAppointmentResult(true, LocalDateTime.parse(response.getStartTime()));
        }
        return new OngoingAppointmentResult(response.getHasOngoing(), null);
    }

    public record OngoingAppointmentResult(boolean hasOngoing, LocalDateTime appointmentTime) {}
}
