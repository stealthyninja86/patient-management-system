package com.pms.scheduleservice.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import notification.OptService;
import notification.OtpServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OtpGrpcClient {

    private static final Logger log =  LoggerFactory.getLogger(OtpGrpcClient.class);

    @GrpcClient("notification-service")
    private OtpServiceGrpc.OtpServiceBlockingStub blockingStub;

    public OptService.GenerateOtpResponse generateOtp(OptService.GenerateOtpRequest request) {
        log.info("Generating OTP: domainKey={}, type={}",
                request.getDomainKey(), request.getNotificationType());
        return blockingStub.generateOtp(request);
    }

    public OptService.VerifyOtpResponse verifyOtp(OptService.VerifyOtpRequest request) {
        log.info("Verifying OTP for domainKey={}", request.getDomainKey());
        return blockingStub.verifyOtp(request);
    }
}
