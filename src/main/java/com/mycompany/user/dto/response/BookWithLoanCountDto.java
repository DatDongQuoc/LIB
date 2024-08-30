package com.mycompany.user.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class BookWithLoanCountDto {

    private Long id;
    private String isbn;
    private String name;
    private String serialName;
    private String description;
    private int quantity;
    private int loanCount;

    public BookWithLoanCountDto(Long id, String isbn, String name, String serialName, String description, int quantity, int loanCount) {
        this.id = id;
        this.isbn = isbn;
        this.name = name;
        this.serialName = serialName;
        this.description = description;
        this.quantity = quantity;
        this.loanCount = loanCount;
    }
}
