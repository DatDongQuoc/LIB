package com.mycompany.user.dto.request;

import lombok.Data;

@Data
public class UserPageRequest {

    private int pageNumber;
    private int pageSize;
    private String firstName;
    private String lastName;
    private String email;
    private Integer Id; // if ID is null, delete field & , in the previous line
}

