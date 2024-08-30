package com.mycompany.user.service;

public interface EmailService {
    void sendEmail(String recipient, String body, String subject);
}
