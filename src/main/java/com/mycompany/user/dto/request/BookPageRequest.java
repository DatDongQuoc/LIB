package com.mycompany.user.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Data

public class BookPageRequest {

    private int pageNumber;
    private int pageSize;
    private String isbn;
    private String name;
    private String serialName;
    private String description;
    private Integer quantity;
    private Set<Long> authorIds;
    private Set<Long> categoryIds;
    private Integer Id; // if ID is null, delete field & , in the previous line
}

