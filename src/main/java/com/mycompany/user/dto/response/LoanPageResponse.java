package com.mycompany.user.dto.response;

import com.mycompany.user.dto.LoanDto;
import lombok.Data;
import java.util.List;

@Data
public class LoanPageResponse {

    private List<LoanDto> loans;
    private int currentPage;
    private long totalItems;
    private int totalPages;

    public LoanPageResponse(List<LoanDto> loans, int currentPage, long totalItems, int totalPages) {
        this.loans = loans;
        this.currentPage = currentPage;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }
}
