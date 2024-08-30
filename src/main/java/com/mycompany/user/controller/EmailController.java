package com.mycompany.user.controller;

import com.mycompany.user.dto.request.EmailRequest;
import com.mycompany.user.dto.response.Response;
import com.mycompany.user.service.EmailService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mycompany.user.constant.ResponseCode.ERROR_CODE;
import static com.mycompany.user.constant.ResponseCode.SUCCESS_CODE;
import static com.mycompany.user.constant.ResponseMessage.EMAIL_SEND_FAILED;
import static com.mycompany.user.constant.ResponseMessage.EMAIL_SENT_SUCCESSFULLY;

@RestController
public class EmailController {

    @Autowired
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/sendEmail")
    public ResponseEntity<Response<String>> sendEmail(@RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmail(emailRequest.getRecipient(), emailRequest.getBody(), emailRequest.getSubject());
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, EMAIL_SENT_SUCCESSFULLY));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new Response<>(ERROR_CODE, EMAIL_SEND_FAILED));
        }
    }
}
