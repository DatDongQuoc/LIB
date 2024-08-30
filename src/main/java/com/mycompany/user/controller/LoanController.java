package com.mycompany.user.controller;

import com.mycompany.user.dto.LoanDto;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.request.ReturnBooksRequest;
import com.mycompany.user.dto.response.LoanPageResponse;
import com.mycompany.user.dto.response.Response;
import com.mycompany.user.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.mycompany.user.constant.ResponseCode.ERROR_CODE;
import static com.mycompany.user.constant.ResponseCode.SUCCESS_CODE;
import static com.mycompany.user.constant.ResponseMessage.*;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/findAll")
    public ResponseEntity<Response<LoanPageResponse>> listLoansByPage(@RequestBody GeneralPageRequest request) {
        try {
            LoanPageResponse loanPageResponse = loanService.findAllLoans(request);
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, LOANS_RETRIEVED_SUCCESSFULLY, loanPageResponse));
        } catch (RuntimeException ex) {
            // Handle any exceptions and return an error response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Response<>(ERROR_CODE, ex.getMessage()));
        }
    }

    @GetMapping("/findLoan")
    public ResponseEntity<Response<LoanDto>> getLoanById(@RequestParam Long id) {
        try {
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, LOAN_RETRIEVED_SUCCESSFULLY, loanService.findLoanById(id)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new Response<>(ERROR_CODE, ex.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Response<String>> createLoan(@RequestBody LoanDto loanDto) {
        try {
            loanService.createLoan(loanDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new Response<>(SUCCESS_CODE, LOAN_CREATED));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Response<>(ERROR_CODE, ex.getMessage()));
        }
    }

    @PutMapping("/return")
    public ResponseEntity<Response<String>> returnBooks(@RequestBody ReturnBooksRequest returnBooksRequest) {
        try {
            loanService.returnBooks(returnBooksRequest);
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, LOANS_RETURNED_SUCCESSFULLY));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Response<>(ERROR_CODE, ex.getMessage()));
        }
    }
}
