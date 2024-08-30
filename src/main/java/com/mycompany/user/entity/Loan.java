package com.mycompany.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static com.mycompany.user.entity.LoanStatus.ACTIVE;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "loans")

public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanStatus status = ACTIVE;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LoanDetail> loanDetails = new HashSet<>();

    public void addLoanDetail(LoanDetail loanDetail) {
        this.loanDetails.add(loanDetail);
        loanDetail.setLoan(this);
    }

    public void removeLoanDetail(LoanDetail loanDetail) {
        this.loanDetails.remove(loanDetail);
        loanDetail.setLoan(null);
    }

    public void updateLoanDetails(Set<LoanDetail> newLoanDetails) {
        this.loanDetails.forEach(this::removeLoanDetail);
        newLoanDetails.forEach(this::addLoanDetail);
    }
}




