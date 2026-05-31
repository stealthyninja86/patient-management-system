package com.pms.scheduleservice.grpc;

import com.pms.scheduleservice.model.Appointment;
import com.pms.scheduleservice.model.AppointmentStatus;
import com.pms.scheduleservice.model.TimeSlot;
import com.pms.scheduleservice.repository.AppointmentRepository;
import com.pms.scheduleservice.repository.TimeSlotRepository;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import schedule.HasOngoingAppointmentRequest;
import schedule.HasOngoingAppointmentResponse;
import schedule.ScheduleServiceGrpc;

import java.io.IOException;
import java.util.Optional;

@Component
public class ScheduleGrpcServer extends ScheduleServiceGrpc.ScheduleServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ScheduleGrpcServer.class);

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private Server server;

    public ScheduleGrpcServer(AppointmentRepository appointmentRepository,
                              TimeSlotRepository timeSlotRepository) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @PostConstruct
    public void start() throws IOException {
        server = ServerBuilder.forPort(9009)
                .addService(this)
                .build()
                .start();
        log.info("gRPC server started on port 9009");
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
            log.info("gRPC server stopped");
        }
    }

    @Override
    public void hasOngoingAppointment(HasOngoingAppointmentRequest request,
                                      StreamObserver<HasOngoingAppointmentResponse> responseObserver) {
        log.debug("Checking ongoing appointment for doctor: {}, patient: {}", request.getDoctorId(), request.getPatientId());

        HasOngoingAppointmentResponse.Builder builder = HasOngoingAppointmentResponse.newBuilder()
                .setHasOngoing(false);

        Optional<Appointment> appointmentOpt = appointmentRepository
                .findFirstByDoctorIdAndPatientIdAndStatus(
                        request.getDoctorId(), request.getPatientId(), AppointmentStatus.ONGOING);

        if (appointmentOpt.isPresent()) {
            builder.setHasOngoing(true);
            timeSlotRepository.findByTimeSlotId(appointmentOpt.get().getTimeSlotId())
                    .ifPresent(slot -> {
                        builder.setStartTime(slot.getStartTime().toString());
                        builder.setEndTime(slot.getEndTime().toString());
                    });
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
