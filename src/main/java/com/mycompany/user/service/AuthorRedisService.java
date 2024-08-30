package com.mycompany.user.service;

import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.response.AuthorPageResponse;

public interface AuthorRedisService {
    AuthorPageResponse getAllAuthors(GeneralPageRequest request);
    void saveAllAuthors(AuthorPageResponse authorPage, GeneralPageRequest request);

}
