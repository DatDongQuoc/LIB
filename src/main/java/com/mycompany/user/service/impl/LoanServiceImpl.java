package com.mycompany.user.service.impl;

import com.mycompany.user.dto.LoanDetailDto;
import com.mycompany.user.dto.LoanDto;
import com.mycompany.user.dto.ReturnedBookDto;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.request.ReturnBooksRequest;
import com.mycompany.user.dto.response.LoanPageResponse;
import com.mycompany.user.entity.*;
import com.mycompany.user.exception.NotFoundException;
import com.mycompany.user.repository.*;
import com.mycompany.user.service.EmailService;
import com.mycompany.user.service.LoanService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.mycompany.user.constant.ResponseCode.ERROR_CODE;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanDetailRepository loanDetailRepository; // Add this
    private final ModelMapper modelMapper;
    private EmailService emailService;

    public LoanServiceImpl(LoanRepository loanRepository, UserRepository userRepository, BookRepository bookRepository, LoanDetailRepository loanDetailRepository, ModelMapper modelMapper, EmailService emailService) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.loanDetailRepository = loanDetailRepository;
        this.modelMapper = modelMapper;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    @Override
    public LoanPageResponse findAllLoans(GeneralPageRequest request) {
        // Set default page number and size if they are invalid
        int pageNumber = request.getPageNumber() < 1 ? 1 : request.getPageNumber();
        int pageSize = request.getPageSize() < 1 ? 10 : request.getPageSize(); // Default page size

        // Create a Pageable object with the page number and size
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        // Fetch the paginated data from the repository
        Page<Loan> page = loanRepository.findAll(pageable); // Use pageable directly

        // Convert the entity list to DTO list
        List<LoanDto> loans = page.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // Return the paginated response
        return new LoanPageResponse(loans, pageNumber, page.getTotalElements(), page.getTotalPages());
    }

    @Transactional(readOnly = true)
    @Override
    public LoanDto findLoanById(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Loan not found with ID %d", id)));
        return convertToDto(loan);
    }

    @Transactional
    public void createLoan(LoanDto loanDto) {
        User user = userRepository.findById((loanDto.getUserId()))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Check the current quantity of borrowed books by the user
        Long currentBorrowedQuantity = loanRepository.sumQuantitiesByUser(user);
        currentBorrowedQuantity = (currentBorrowedQuantity != null) ? currentBorrowedQuantity : 0;

        // Filter out LoanDetailDto entries with quantity of 0
        Set<LoanDetailDto> validLoanDetails = loanDto.getLoanDetails().stream()
                .filter(detailDto -> detailDto.getQuantity() > 0)
                .collect(Collectors.toSet());

        // Calculate the number of books the user wants to borrow
        long newBooksQuantity = validLoanDetails.stream()
                .mapToLong(LoanDetailDto::getQuantity)
                .sum();

        // Calculate the remaining borrow limit
        long maxBorrowLimit = 5;
        long remainingBorrowLimit = maxBorrowLimit - currentBorrowedQuantity;

        if (newBooksQuantity > remainingBorrowLimit) {
            if (remainingBorrowLimit <= 0) {
                throw new IllegalStateException("User cannot borrow any more books.");
            } else {
                throw new IllegalStateException("User cannot borrow more than " + remainingBorrowLimit + " additional books.");
            }
        }

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setLoanDate(loanDto.getLoanDate());
        loan.setStatus(LoanStatus.valueOf(String.valueOf(loanDto.getStatus()))); // Ensure the status is set correctly

        Set<LoanDetail> loanDetails = validLoanDetails.stream().map(detailDto -> {
            Book book = bookRepository.findById(detailDto.getBookId())
                    .orElseThrow(() -> new EntityNotFoundException("Book not found"));

            // Check if there is enough quantity for the book
            if (book.getQuantity() < detailDto.getQuantity()) {
                throw new IllegalArgumentException(String.format("Not enough quantity available for book ID %d", detailDto.getBookId()));
            }

            LoanDetail loanDetail = new LoanDetail();
            loanDetail.setBook(book);

            loanDetail.setInitialQuantity(detailDto.getQuantity()); // Set the initial quantity
            loanDetail.setQuantity(detailDto.getQuantity());
            loanDetail.setDueDate(detailDto.getDueDate()); // Set individual due date
            loanDetail.setFineAmount(detailDto.getFineAmount());// Set individual fine amount
            loanDetail.setLoan(loan); // Set the loan field here

            // Update the book quantity
            book.setQuantity(book.getQuantity() - detailDto.getQuantity());
            bookRepository.save(book); // Save the updated book

            return loanDetail;
        }).collect(Collectors.toSet());

        loan.setLoanDetails(loanDetails);

        loanRepository.save(loan);
    }

    @Transactional
    public void returnBooks(ReturnBooksRequest returnBooksRequest) {
        // Retrieve the existing loan
        Loan loan = loanRepository.findById(returnBooksRequest.getLoanId())
                .orElseThrow(() -> new NotFoundException("Loan not found"));

        // Check if the loan is already inactive
        if (loan.getStatus() == LoanStatus.INACTIVE) {
            throw new IllegalStateException("Loan has already been returned.");
        }

        // Iterate through the returned books
        for (ReturnedBookDto returnedBookDto : returnBooksRequest.getReturnedBooks()) {
            Book book = bookRepository.findById(returnedBookDto.getBookId())
                    .orElseThrow(() -> new EntityNotFoundException("Book not found"));

            // Retrieve the corresponding LoanDetail
            LoanDetail loanDetail = loan.getLoanDetails().stream()
                    .filter(detail -> detail.getBook().getId().equals(returnedBookDto.getBookId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("LoanDetail not found"));

            // Update the quantity in LoanDetail
            int newQuantity = loanDetail.getQuantity() - returnedBookDto.getQuantity();
            if (newQuantity < 0) {
                throw new IllegalStateException("Return quantity exceeds borrowed quantity.");
            }
            loanDetail.setQuantity(newQuantity);

            // Update the quantity in Book
            book.setQuantity(book.getQuantity() + returnedBookDto.getQuantity());
            bookRepository.save(book);

            // Calculate fine if overdue
            if (loanDetail.getDueDate() != null) {
                long daysOverdue = ChronoUnit.DAYS.between(loanDetail.getDueDate(), LocalDate.now());
                if (daysOverdue > 0) {
                    double fineAmount = loanDetail.getFineAmount() + calculateFine(daysOverdue);
                    loanDetail.setFineAmount(fineAmount);
                }
            }
        }

        // Update the loan status to INACTIVE if all books are returned
        boolean allBooksReturned = loan.getLoanDetails().stream()
                .allMatch(detail -> detail.getQuantity() == 0);

        if (allBooksReturned) {
            loan.setStatus(LoanStatus.INACTIVE);
        }

        // Save the updated loan and its details
        loanRepository.save(loan);
    }

    private double calculateFine(long daysOverdue) {
        // Example fine calculation logic
        return daysOverdue * 1.0; // $1 per day overdue
    }

    private LoanDto convertToDto(Loan loan) {
        return modelMapper.map(loan, LoanDto.class);
    }

    private Loan convertToEntity(LoanDto loanDto) {
        return modelMapper.map(loanDto, Loan.class);
    }
}
