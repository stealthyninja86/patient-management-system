package com.pms.notificationservice.controller;

import com.pms.notificationservice.dto.NotificationRequest;
import com.pms.notificationservice.dto.SendNotificationResponseDTO;
import com.pms.notificationservice.facade.NotificationFacade;
import com.pms.notificationservice.model.Notification;
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
    public ResponseEntity<List<Notification>> getPatientNotificationHistory(
            @PathVariable String patientId
    ){
        List<Notification> notifications = notificationFacade.getPatientNotificationHistory(patientId);
        return ResponseEntity.ok().body(notifications);
    }
}
