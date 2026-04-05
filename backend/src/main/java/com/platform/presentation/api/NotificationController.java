package com.platform.presentation.api;

import com.platform.application.NotificationService;
import com.platform.domain.Notification;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{recipientId}")
    public List<Notification> getNotifications(@PathVariable String recipientId) {
        return notificationService.getNotifications(recipientId);
    }
}
