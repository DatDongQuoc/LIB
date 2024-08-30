package com.mycompany.user.dto.response;

import com.mycompany.user.dto.UserDto;
import java.util.List;
import lombok.Data;

@Data
public class  UserPageResponse {

    private List<UserDto> users;
    private int currentPage;
    private long totalItems;
    private int totalPages;

    // Constructors
    public UserPageResponse(List<UserDto> users, int currentPage, long totalItems, int totalPages) {
        this.users = users;
        this.currentPage = currentPage;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }
}
