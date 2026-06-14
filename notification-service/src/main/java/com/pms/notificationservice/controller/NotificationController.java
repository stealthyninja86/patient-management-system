package com.pms.notificationservice.controller;

import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.dto.response.NotificationResponseDTO;
import com.pms.notificationservice.dto.response.SendNotificationResponseDTO;
import com.pms.notificationservice.service.facade.NotificationFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationFacade notificationFacade;

    public NotificationController(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    @PostMapping("/send")
    public ResponseEntity<SendNotificationResponseDTO> sendNotification(
            @RequestBody NotificationRequest request
    ){
        boolean sent = notificationFacade.sendNotification(request);
        return ResponseEntity.ok(new SendNotificationResponseDTO(
            sent, sent ? "Notification sent" : "Duplicate suppressed"));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<NotificationResponseDTO>> getPatientNotificationHistory(
            @PathVariable String patientId
    ){
        List<NotificationResponseDTO> notifications = notificationFacade.getPatientNotificationHistory(patientId);
        return ResponseEntity.ok().body(notifications);
    }
}
