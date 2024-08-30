package com.mycompany.user.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Data

public class LoanDetailDto {
    private Long id;
    private Long bookId;
    private Integer initialQuantity; // Initial quantity at the time of loan
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;
    private LocalDate dueDate; // Due date specific to this book
    private Double fineAmount; // Fine amount specific to this book
}

