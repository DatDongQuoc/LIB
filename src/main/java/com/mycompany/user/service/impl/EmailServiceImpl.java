package com.mycompany.user.service.impl;

import com.mycompany.user.entity.EmailRecord;
import com.mycompany.user.entity.LoanDetail;
import com.mycompany.user.entity.User;
import com.mycompany.user.repository.EmailRecordRepository;
import com.mycompany.user.repository.LoanDetailRepository;
import com.mycompany.user.repository.LoanRepository;
import com.mycompany.user.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailRecordRepository emailRecordRepository;
    private final LoanDetailRepository loanDetailRepository;

    @Value("${spring.mail.username}")
    private String fromEmailId;

    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender, EmailRecordRepository emailRecordRepository, LoanDetailRepository loanDetailRepository) {
        this.javaMailSender = javaMailSender;
        this.emailRecordRepository = emailRecordRepository;
        this.loanDetailRepository = loanDetailRepository;
    }

    @Override
    public void sendEmail(String recipient, String body, String subject) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromEmailId);
        simpleMailMessage.setTo(recipient);
        simpleMailMessage.setText(body);
        simpleMailMessage.setSubject(subject);
        javaMailSender.send(simpleMailMessage);
    }

    private static final Logger logger = LoggerFactory.getLogger("com.mycompany.alerts");

    @Transactional
    @Scheduled(cron = "0 04 14 * * MON,SAT") // Adjust cron expression as needed
    public void collectAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate startOfDueSoon = today.plusDays(1);
        LocalDate endOfDueSoon = today.plusDays(3);

        logger.info("Collecting alerts for date: {}", today);
        logger.info("Due soon range: {} to {}", startOfDueSoon, endOfDueSoon);

        Map<User, List<LoanDetail>> dueSoonMap = new HashMap<>();
        Map<User, List<LoanDetail>> overdueMap = new HashMap<>();

        List<LoanDetail> dueSoonDetails = loanDetailRepository.findDueSoonLoanDetails(startOfDueSoon, endOfDueSoon);
        logger.info("Found {} due soon loan details", dueSoonDetails.size());
        for (LoanDetail loanDetail : dueSoonDetails) {
            addToMap(dueSoonMap, loanDetail);
        }

        List<LoanDetail> overdueDetails = loanDetailRepository.findOverdueLoanDetails(today);
        logger.info("Found {} overdue loan details", overdueDetails.size());
        for (LoanDetail loanDetail : overdueDetails) {
            addToMap(overdueMap, loanDetail);
        }

        saveEmailRecords(dueSoonMap, overdueMap);
    }

    private void addToMap(Map<User, List<LoanDetail>> map, LoanDetail loanDetail) {
        User user = loanDetail.getLoan().getUser();
        if (loanDetail.getQuantity() > 0) {
            map.computeIfAbsent(user, k -> new ArrayList<>()).add(loanDetail);
        }
    }

    private void saveEmailRecords(Map<User, List<LoanDetail>> dueSoonMap, Map<User, List<LoanDetail>> overdueMap) {
        for (Map.Entry<User, List<LoanDetail>> entry : dueSoonMap.entrySet()) {
            User user = entry.getKey();
            List<LoanDetail> dueSoonDetails = entry.getValue();
            List<LoanDetail> overdueDetails = overdueMap.getOrDefault(user, Collections.emptyList());

            StringBuilder body = new StringBuilder();
            body.append(String.format("Dear %s,\n\n", user.getFirstName()));
            body.append("This is a reminder that the following loans are due soon and some are overdue:\n\n");

            if (!dueSoonDetails.isEmpty()) {
                body.append("Loans due in the next 1-3 day(s):\n\n");
                for (LoanDetail loanDetail : dueSoonDetails) {
                    body.append(String.format("- Book ID: %d, Quantity: %d, Due Date: %s\n",
                            loanDetail.getBook().getId(),
                            loanDetail.getQuantity(),
                            loanDetail.getDueDate()));
                }
            } else {
                body.append("No loans due soon.\n\n");
            }

            if (!overdueDetails.isEmpty()) {
                body.append("\nLoans overdue:\n\n");
                for (LoanDetail loanDetail : overdueDetails) {
                    body.append(String.format("- Book ID: %d, Quantity: %d, Due Date: %s\n",
                            loanDetail.getBook().getId(),
                            loanDetail.getQuantity(),
                            loanDetail.getDueDate()));
                }
            } else {
                body.append("No overdue loans.\n\n");
            }

            body.append("\nPlease return the books before the due date to avoid any late fees and return the overdue books as soon as possible to avoid additional late fees.\n\n");
            body.append("Thank you,\nLibrary Management System");

            EmailRecord emailRecord = new EmailRecord();
            emailRecord.setRecipient(user.getEmail());
            emailRecord.setSubject("Loan Reminder");
            emailRecord.setBody(body.toString());
            emailRecord.setStatus("PENDING");
            emailRecord.setDate(LocalDate.now()); // Set the sent date here
            emailRecord.setRetryCount(0); // Initialize retry count

            emailRecordRepository.save(emailRecord);

            logger.info("Email details collected for user {}: \n{}", user.getEmail(), body);
        }
    }

    @Transactional
    @Scheduled(cron = "0 06 14 * * MON,SAT") // Adjust cron expression as needed
    public void sendCollectedEmails() {
        List<EmailRecord> emailRecords = emailRecordRepository.findByStatus("PENDING");
        for (EmailRecord emailRecord : emailRecords) {
            boolean sent = false;
            for (int i = 0; i < 3; i++) {
                try {
                    sendEmail(emailRecord.getRecipient(), emailRecord.getBody(), emailRecord.getSubject());
                    emailRecord.setStatus("SENT");
                    emailRecord.setDate(LocalDate.now());
                    emailRecordRepository.save(emailRecord);
                    sent = true;
                    break; // Exit loop if email is successfully sent
                } catch (Exception e) {
                    logger.error("Failed to send email to {}. Attempt {} of 3", emailRecord.getRecipient(), i + 1, e);
                    emailRecord.setRetryCount(emailRecord.getRetryCount() + 1);
                    emailRecord.setStatus("FAILED");
                    emailRecordRepository.save(emailRecord);
                    if (i == 2) {
                        logger.error("Failed to send email to {} after 3 attempts", emailRecord.getRecipient());
                    }
                }
            }
            if (!sent) {
                // Handle the case where the email could not be sent after retries
                emailRecord.setStatus("FAILED");
                emailRecordRepository.save(emailRecord);
            }
        }
    }
}

