package com.mycompany.user.dto.response;

import com.mycompany.user.dto.BookDto;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BookPageResponse implements Serializable {

    private List<BookDto> books;
    private int currentPage;
    private long totalItems;
    private int totalPages;

    public BookPageResponse(List<BookDto> books, int currentPage, long totalItems, int totalPages) {
        this.books = books;
        this.currentPage = currentPage;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }
}
