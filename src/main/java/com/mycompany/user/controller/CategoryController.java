package com.mycompany.user.controller;

import com.mycompany.user.dto.CategoryDto;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.response.CategoryPageResponse;
import com.mycompany.user.exception.CustomException;
import com.mycompany.user.exception.NotFoundException;
import com.mycompany.user.service.CategoryService;
import com.mycompany.user.dto.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.mycompany.user.constant.ResponseCode.ERROR_CODE;
import static com.mycompany.user.constant.ResponseCode.SUCCESS_CODE;
import static com.mycompany.user.constant.ResponseMessage.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/findAll")
    public ResponseEntity<Response<CategoryPageResponse>> listCategoriesByPage(@RequestBody GeneralPageRequest request) {
        try {
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, CATEGORIES_RETRIEVED, categoryService.getCategoryListByPage(request)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Response<>(ERROR_CODE, ex.getMessage()));
        }
    }

    @GetMapping("/findById")
    public ResponseEntity<Response<CategoryDto>> findCategoryById(@RequestParam Long id) {
        try {
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, CATEGORY_RETRIEVED, categoryService.findCategoryById(id)));
        } catch (NotFoundException e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Response<String>> createCategory(@RequestBody CategoryDto categoryDto) {
        try {
            categoryService.createCategory(categoryDto);
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, CATEGORY_CREATED));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, "Failed to create category: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Response<String>> updateCategory(@RequestBody CategoryDto categoryDto) {
        try {
            categoryService.updateCategory(categoryDto);
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, CATEGORY_UPDATED_SUCCESSFULLY));
        } catch (NotFoundException e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, "Failed to update category: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Response<String>> deleteCategory(@RequestParam Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, CATEGORY_DELETED_SUCCESSFULLY));
        } catch (CustomException e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, "Failed to delete category: " + e.getMessage()));
        }
    }
}
