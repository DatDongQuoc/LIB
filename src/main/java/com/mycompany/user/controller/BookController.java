package com.mycompany.user.controller;

import com.mycompany.user.dto.request.BookPageRequest;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.response.BookPageResponse;
import com.mycompany.user.dto.response.CustomPageResponse;
import com.mycompany.user.dto.response.Response;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.mycompany.user.dto.BookDto;
import com.mycompany.user.service.AuthorService;
import com.mycompany.user.service.BookService;
import com.mycompany.user.service.CategoryService;

import static com.mycompany.user.constant.ResponseCode.ERROR_CODE;
import static com.mycompany.user.constant.ResponseCode.SUCCESS_CODE;
import static com.mycompany.user.constant.ResponseMessage.*;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, AuthorService authorService, CategoryService categoryService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.categoryService = categoryService;
    }

    @PostMapping("/findAll")
    public ResponseEntity<Response<BookPageResponse>> findAllBooks(
            @RequestBody GeneralPageRequest pageRequest) {
        try {
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, BOOKS_RETRIEVED, bookService.findAllBooks(pageRequest)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Response<>(ERROR_CODE, ex.getMessage()));
        }
    }

    @PostMapping("/page")
    public ResponseEntity<Response<BookPageResponse>> listBooksByPage(@RequestBody BookPageRequest request) {
        try {
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, BOOKS_RETRIEVED, bookService.getBookListByPage(request)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Response<>(ERROR_CODE, ex.getMessage()));
        }
    }

    @GetMapping("findBook")
    public ResponseEntity<Response<BookDto>> findBookById(@RequestParam Long id) {
        return ResponseEntity.ok(
                new Response<>(SUCCESS_CODE, BOOK_RETRIEVED, bookService.findBookById(id)));
    }

    @PostMapping("/add")
    public ResponseEntity<Response<String>> createBook(@RequestBody BookDto bookDto, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, VALIDATION_ERROR));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new Response<>(SUCCESS_CODE, BOOK_CREATED_SUCCESSFULLY,bookService.createBook(bookDto)));
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateBook(@Valid @RequestBody BookDto bookDto, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, UPDATE_ERROR));
        }
        return ResponseEntity.ok().body(
                new Response<>(SUCCESS_CODE, BOOK_UPDATED_SUCCESSFULLY, bookService.updateBook(bookDto)));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Response<String>> deleteBook(@RequestParam Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, BOOK_DELETED_SUCCESSFULLY));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, ex.getMessage()));
        }
    }

    @PostMapping("/most-borrowed")
    public ResponseEntity<Response<CustomPageResponse<BookDto>>> getMostBorrowedBooks(
            @RequestBody GeneralPageRequest pageRequest) {
        return ResponseEntity.ok(
                new Response<>(SUCCESS_CODE, MOST_LOANED_BOOKS_RETRIEVED, bookService.getMostBorrowedBooks(pageRequest)));
    }

    @PostMapping("/running-low")
    public ResponseEntity<Response<CustomPageResponse<BookDto>>> getBooksRunningLow(
            @RequestParam int threshold, @RequestBody GeneralPageRequest pageRequest) {
        return ResponseEntity.ok(
                new Response<>(SUCCESS_CODE, BOOKS_RUNNING_LOW_RETRIEVED, bookService.findBooksRunningLow(threshold, pageRequest)));
    }
}