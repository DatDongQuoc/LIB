package com.mycompany.user.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter

public class GeneralPageRequest implements Serializable {

    private int pageNumber = 1; // Default to page 1
    private int pageSize = 10;  // Default to page size of 10
}
