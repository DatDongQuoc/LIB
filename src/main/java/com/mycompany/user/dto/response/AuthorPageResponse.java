package com.mycompany.user.dto.response;

import com.mycompany.user.dto.AuthorDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class AuthorPageResponse {

    private List<AuthorDto> authors;
    private int currentPage;
    private long totalElements;
    private int totalPages;

    public AuthorPageResponse(List<AuthorDto> authors, int currentPage, long totalElements, int totalPages) {
        this.authors = authors;
        this.currentPage = currentPage;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}
