package com.example.pim.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendNotification(String recipient, String subject, String message) {
        // TODO: Integrate with an actual notification system (e.g., email, messaging queue)
        System.out.println("Sending notification to: " + recipient);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        // For now, just log the notification
    }
}
