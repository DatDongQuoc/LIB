package com.mycompany.user.repository;

import com.mycompany.user.entity.EmailRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailRecordRepository extends JpaRepository<EmailRecord, Long> {
    List<EmailRecord> findByStatus(String status);
    Optional<EmailRecord> findByRecipientAndDate(String recipient, LocalDate date);
}

