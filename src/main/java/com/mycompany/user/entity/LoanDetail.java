package com.mycompany.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "loan_details")

public class LoanDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "initial_quantity", nullable = false)
    private Integer initialQuantity; // New field to store the original quantity

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "fine_amount")
    private Double fineAmount;
}



