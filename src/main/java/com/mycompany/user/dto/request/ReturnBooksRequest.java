package com.mycompany.user.dto.request;

import com.mycompany.user.dto.ReturnedBookDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ReturnBooksRequest {

    private Long loanId;
    private Set<ReturnedBookDto> returnedBooks;
}
