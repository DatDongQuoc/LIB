package com.mycompany.user.repository;

import com.mycompany.user.entity.LoanDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanDetailRepository extends JpaRepository<LoanDetail, Long> {

    @Query("SELECT ld FROM LoanDetail ld  WHERE ld.dueDate BETWEEN :startDate AND :endDate AND ld.quantity > 0")
    List<LoanDetail> findDueSoonLoanDetails(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ld FROM LoanDetail ld  WHERE ld.dueDate <= :today AND ld.quantity > 0")
    List<LoanDetail> findOverdueLoanDetails(@Param("today") LocalDate today);

}
