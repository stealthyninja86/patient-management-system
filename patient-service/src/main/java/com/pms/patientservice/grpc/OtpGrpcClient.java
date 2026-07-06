package com.pms.patientservice.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import notification.OptService;
import notification.OtpServiceGrpc;
import org.springframework.stereotype.Component;

@Component
public class OtpGrpcClient {

    @GrpcClient("notification-service")
    private OtpServiceGrpc.OtpServiceBlockingStub otpServiceBlockingStub;

    public OptService.GenerateOtpResponse generateOtp(OptService.GenerateOtpRequest generateOtpRequest) {
        return otpServiceBlockingStub.generateOtp(generateOtpRequest);
    }

    public OptService.VerifyOtpResponse verifyOtp(OptService.VerifyOtpRequest verifyOtpRequest) {
        return otpServiceBlockingStub.verifyOtp(verifyOtpRequest);
    }
}
