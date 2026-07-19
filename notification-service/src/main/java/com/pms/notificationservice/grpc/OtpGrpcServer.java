package com.pms.notificationservice.grpc;

import com.pms.notificationservice.dto.response.OtpVerifyResult;
import com.pms.notificationservice.exception.OtpCoolDownException;
import com.pms.notificationservice.model.NotificationType;
import com.pms.notificationservice.service.OtpService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import notification.OptService;
import notification.OtpServiceGrpc;

@GrpcService
public class OtpGrpcServer extends OtpServiceGrpc.OtpServiceImplBase {
    private final OtpService otpService;

    public OtpGrpcServer(OtpService otpService) {
        this.otpService = otpService;
    }

    @Override
    public void generateOtp(OptService.GenerateOtpRequest request,
                            StreamObserver<OptService.GenerateOtpResponse> responseObserver) {
        try{
            NotificationType type = NotificationType.valueOf(request.getNotificationType());

            otpService.generateOtpForDomain(
                    request.getDomainKey(),
                    request.getPhoneNumber(),
                    request.getEmail(),
                    type
            );

           OptService.GenerateOtpResponse response = OptService.GenerateOtpResponse.newBuilder()
                    .setSuccess(true)
                    .setTtlSeconds(180)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }catch (OtpCoolDownException e){
            responseObserver.onError(
                    Status.RESOURCE_EXHAUSTED.withDescription(e.getMessage()).asRuntimeException()
            );
        }
        catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Failed to generate OTP: " + e.getMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void verifyOtp(OptService.VerifyOtpRequest request,
                          StreamObserver<OptService.VerifyOtpResponse> responseObserver) {
        try {
            OtpVerifyResult result = otpService.verifyOtpForDomain(
                        request.getDomainKey(),
                        request.getCode()
                    );

            OptService.VerifyOtpResponse response = OptService.VerifyOtpResponse.newBuilder()
                    .setVerified(result.verified())
                    .setStatus(result.status())
                    .setAttemptsRemaining(result.attemptsRemaining())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Failed to verify OTP: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }
}
