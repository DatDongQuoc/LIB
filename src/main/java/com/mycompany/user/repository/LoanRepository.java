package com.mycompany.user.repository;

import com.mycompany.user.entity.Loan;
import com.mycompany.user.entity.LoanStatus;
import com.mycompany.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    long countByUserId(Long userId);

    // Custom query to sum the quantities of borrowed books for a specific user
    @Query("SELECT (SUM(ld.quantity)) FROM Loan l JOIN l.loanDetails ld WHERE l.user = :user")
    Long sumQuantitiesByUser(@Param("user") User user);
}
