package com.mycompany.user.service;

import com.mycompany.user.dto.CategoryDto;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.response.CategoryPageResponse;

public interface CategoryService {

    CategoryPageResponse getCategoryListByPage(GeneralPageRequest request);

    CategoryDto findCategoryById(Long id);

    void createCategory(CategoryDto categoryDto);

    void updateCategory(CategoryDto categoryDto);

    void deleteCategory(Long id);
}
