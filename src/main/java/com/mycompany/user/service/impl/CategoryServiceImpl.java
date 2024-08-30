package com.mycompany.user.service.impl;

import com.mycompany.user.dto.CategoryDto;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.response.CategoryPageResponse;
import com.mycompany.user.entity.Category;
import com.mycompany.user.exception.NotFoundException;
import com.mycompany.user.repository.CategoryRepository;
import com.mycompany.user.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;
	private final ModelMapper modelMapper;

	public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
		this.categoryRepository = categoryRepository;
		this.modelMapper = modelMapper;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	@Cacheable(value = "categories", key = "#request.pageNumber + '-' + #request.pageSize")
	public CategoryPageResponse getCategoryListByPage(GeneralPageRequest request) {
		int pageNumber = request.getPageNumber() < 1 ? 1 : request.getPageNumber();
		int pageSize = request.getPageSize() < 1 ? 10 : request.getPageSize(); // Default page size

		Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
		Page<Category> page = categoryRepository.findAll(pageable); // Use pageable directly

		List<CategoryDto> categories = page.getContent().stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());

		return new CategoryPageResponse(categories, pageNumber, page.getTotalElements(), page.getTotalPages());
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	@Cacheable(value = "categories", key = "#id")
	public CategoryDto findCategoryById(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(String.format("Category not found with ID %d", id)));
		return convertToDto(category);
	}

	@Transactional
	@Override
	public void createCategory(CategoryDto categoryDto) {
		// Convert DTO to entity
		Category category = convertToEntity(categoryDto);
		// Ensure ID is not set for new categories
		if (category.getId() != null) {
			throw new IllegalArgumentException("ID should not be set for new categories");
		}
		categoryRepository.save(category);
	}

	@Transactional
	@Override
	@CachePut(value = "categories", key = "#categoryDto.id")
	public void updateCategory(CategoryDto categoryDto) {
		// Check if the category exists before updating
		if (!categoryRepository.existsById(categoryDto.getId())) {
			throw new NotFoundException(String.format("Category not found with ID %d", categoryDto.getId()));
		}
		// Convert DTO to entity and save
		Category category = convertToEntity(categoryDto);
		categoryRepository.save(category);
	}

	@Transactional
	@Override
	@CacheEvict(value = "categories", key = "#id")
	public void deleteCategory(Long id) {
		// Check if the category exists before deleting
		if (!categoryRepository.existsById(id)) {
			throw new NotFoundException(String.format("Category not found with ID %d", id));
		}
		categoryRepository.deleteById(id);
	}

	private CategoryDto convertToDto(Category category) {
		return modelMapper.map(category, CategoryDto.class);
	}

	private Category convertToEntity(CategoryDto categoryDto) {
		return modelMapper.map(categoryDto, Category.class);
	}
}
