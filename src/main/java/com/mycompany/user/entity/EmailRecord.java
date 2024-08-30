package com.mycompany.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity

public class EmailRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipient;
    private String subject;

    @Column(columnDefinition = "LONGTEXT")
    private String body; // Ensure this field is mapped as a large object

    private String status; // PENDING, SENT, FAILED

    private int retryCount = 0; // To track the number of retries

    private LocalDate date; // Add this field
}


