package com.mycompany.user.controller;

import com.mycompany.user.dto.AuthorDto;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.response.AuthorPageResponse;
import com.mycompany.user.exception.CustomException;
import com.mycompany.user.exception.NotFoundException;
import com.mycompany.user.service.AuthorRedisService;
import com.mycompany.user.service.AuthorService;
import com.mycompany.user.dto.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.mycompany.user.constant.ResponseCode.ERROR_CODE;
import static com.mycompany.user.constant.ResponseCode.SUCCESS_CODE;
import static com.mycompany.user.constant.ResponseMessage.*;

@RestController
@RequestMapping("/authors")
public class AuthorController {

    private final AuthorService authorService;
    private final AuthorRedisService authorRedisService; // Inject the Redis service

    public AuthorController(AuthorService authorService, AuthorRedisService authorRedisService) {
        this.authorService = authorService;
        this.authorRedisService = authorRedisService;
    }

    @PostMapping("/findAll")
    public ResponseEntity<Response<AuthorPageResponse>> listAuthorsByPage(@RequestBody GeneralPageRequest request) {
        try {
            AuthorPageResponse authorPageInRedis = authorRedisService.getAllAuthors(request);
            if (authorPageInRedis == null) {
                AuthorPageResponse authorPage = authorService.getAuthorListByPage(request);
                authorRedisService.saveAllAuthors(authorPage, request);
                return ResponseEntity.ok(
                        new Response<>(SUCCESS_CODE, AUTHORS_RETRIEVED, authorPage));
            } else {
                return ResponseEntity.ok(
                        new Response<>(SUCCESS_CODE, AUTHORS_RETRIEVED, authorPageInRedis));
            }
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Response<>(ERROR_CODE, ex.getMessage()));
        }
    }

    @GetMapping("/findById")
    public ResponseEntity<Response<AuthorDto>> findAuthorById(@RequestParam Long id) {
        try {
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, AUTHORS_RETRIEVED, authorService.findAuthorById(id)));
        } catch (NotFoundException e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Response<String>> createAuthor(@RequestBody AuthorDto authorDto) {
        try {
            authorService.createAuthor(authorDto);
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, AUTHORS_CREATED));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, "Error creating author: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Response<String>> updateAuthor(@RequestBody AuthorDto authorDto) {
        try {
            authorService.updateAuthor(authorDto);
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, AUTHOR_UPDATED_SUCCESSFULLY));
        } catch (NotFoundException e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, "Error updating author: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Response<String>> deleteAuthor(@RequestParam Long id) {
        try {
            authorService.deleteAuthor(id);
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, AUTHOR_DELETED_SUCCESSFULLY));
        } catch (CustomException e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, "Error deleting author: " + e.getMessage()));
        }
    }
}
