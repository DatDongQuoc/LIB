package com.mycompany.user.service;

import com.mycompany.user.dto.BookDto;
import com.mycompany.user.dto.request.BookPageRequest;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.response.BookPageResponse;
import com.mycompany.user.dto.response.CustomPageResponse;

public interface BookService {

    BookPageResponse findAllBooks(GeneralPageRequest pageRequest);

    BookPageResponse getBookListByPage(BookPageRequest request);

    BookDto findBookById(Long id);

    String createBook(BookDto bookDto);

    String updateBook(BookDto bookDto);

    void deleteBook(Long id);

    CustomPageResponse<BookDto> getMostBorrowedBooks(GeneralPageRequest pageRequest);

    CustomPageResponse<BookDto> findBooksRunningLow(int threshold, GeneralPageRequest pageRequest);
}
