package com.pms.scheduleservice.grpc;

import com.pms.scheduleservice.model.Appointment;
import com.pms.scheduleservice.model.AppointmentStatus;
import com.pms.scheduleservice.model.TimeSlot;
import com.pms.scheduleservice.repository.AppointmentRepository;
import com.pms.scheduleservice.repository.TimeSlotRepository;
import java.util.List;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schedule.HasOngoingAppointmentRequest;
import schedule.HasOngoingAppointmentResponse;
import schedule.ScheduleServiceGrpc;

import java.util.Optional;

@GrpcService
public class ScheduleGrpcServer extends ScheduleServiceGrpc.ScheduleServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ScheduleGrpcServer.class);

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;

    public ScheduleGrpcServer(AppointmentRepository appointmentRepository,
                              TimeSlotRepository timeSlotRepository) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Override
    public void hasOngoingAppointment(HasOngoingAppointmentRequest request,
                                      StreamObserver<HasOngoingAppointmentResponse> responseObserver) {
        log.debug("Checking ongoing appointment for doctor: {}, patient: {}", request.getDoctorId(), request.getPatientId());

        HasOngoingAppointmentResponse.Builder builder = HasOngoingAppointmentResponse.newBuilder()
                .setHasOngoing(false);

        Optional<Appointment> appointmentOpt = appointmentRepository
                .findFirstByDoctorIdAndPatientIdAndStatusIn(
                        request.getDoctorId(), request.getPatientId(),
                        List.of(AppointmentStatus.BOOKED, AppointmentStatus.ONGOING, AppointmentStatus.COMPLETED));

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
