package com.mycompany.user.service;

import com.mycompany.user.dto.LoanDto;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.request.ReturnBooksRequest;
import com.mycompany.user.dto.response.LoanPageResponse;

import java.util.List;

public interface LoanService {
    LoanPageResponse findAllLoans(GeneralPageRequest request);
    LoanDto findLoanById(Long id);
    void createLoan(LoanDto loanDto);
    void returnBooks(ReturnBooksRequest returnBooksRequest);
}
