package com.mycompany.user.dto;

import com.mycompany.user.entity.LoanStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter

public class LoanDto {
    private Long id;
    private Long userId;
    private LocalDate loanDate;
    private LoanStatus status; // Consider using LoanStatus enum if possible
    private Set<LoanDetailDto> loanDetails; // List of loan details
}


