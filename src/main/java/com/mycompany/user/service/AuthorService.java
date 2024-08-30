package com.mycompany.user.service;

import com.mycompany.user.dto.AuthorDto;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.response.AuthorPageResponse;

public interface AuthorService {

    AuthorPageResponse getAuthorListByPage(GeneralPageRequest request);

    AuthorDto findAuthorById(Long id);

    void createAuthor(AuthorDto authorDto);

    void updateAuthor(AuthorDto authorDto);

    void deleteAuthor(Long id);
}
