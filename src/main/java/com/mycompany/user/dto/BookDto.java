package com.mycompany.user.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class BookDto implements Serializable {

    private Long id;
    private String isbn;
    private String name;
    private String serialName;
    private String description;
    @Min(value = 0, message = "Quantity must be at least 0")
    private Integer quantity;
    private Set<Long> authorIds = new HashSet<>(); // Store author IDs
    private Set<Long> categoryIds = new HashSet<>(); // Store category IDs
    private Long borrowCount; // Field for the number of times the book has been borrowed

    public BookDto() {
    }

    public BookDto(Long id, String isbn, String name, String serialName, String description,
                   Integer quantity, Set<Long> authorIds, Set<Long> categoryIds, Long borrowCount) {
        this.id = id;
        this.isbn = isbn;
        this.name = name;
        this.serialName = serialName;
        this.description = description;
        this.quantity = quantity;
        this.authorIds = authorIds;
        this.categoryIds = categoryIds;
        this.borrowCount = borrowCount;
    }
}
