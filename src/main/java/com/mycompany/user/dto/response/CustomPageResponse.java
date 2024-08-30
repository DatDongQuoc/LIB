package com.mycompany.user.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter

public class CustomPageResponse<T> implements Serializable {
    private List<T> content; // The list of items on the current page
    private int totalElements; // Total number of elements across all pages
    private int totalPages; // Total number of pages
    private int currentPage; // Current page number

    public CustomPageResponse(List<T> content, int totalElements, int totalPages, int currentPage) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }
}
