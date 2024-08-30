package com.mycompany.user.dto.response;

import com.mycompany.user.dto.CategoryDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter

public class CategoryPageResponse implements Serializable {
    private List<CategoryDto> categories;
    private int currentPage;
    private long totalElements;
    private int totalPages;

    public CategoryPageResponse(List<CategoryDto> categories, int currentPage, long totalElements, int totalPages) {
        this.categories = categories;
        this.currentPage = currentPage;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}
