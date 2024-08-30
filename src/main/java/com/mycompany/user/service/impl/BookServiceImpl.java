package com.mycompany.user.service.impl;

import com.mycompany.user.dto.BookDto;
import com.mycompany.user.dto.request.BookPageRequest;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.response.BookPageResponse;
import com.mycompany.user.dto.response.BookWithLoanCountDto;
import com.mycompany.user.dto.response.CustomPageResponse;
import com.mycompany.user.entity.Book;
import com.mycompany.user.entity.Author;
import com.mycompany.user.entity.Category;
import com.mycompany.user.entity.LoanStatus;
import com.mycompany.user.exception.NotFoundException;
import com.mycompany.user.repository.AuthorRepository;
import com.mycompany.user.repository.BookRepository;
import com.mycompany.user.repository.CategoryRepository;
import com.mycompany.user.repository.LoanRepository;
import com.mycompany.user.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

	private final BookRepository bookRepository;
	private final AuthorRepository authorRepository;
	private final CategoryRepository categoryRepository;
	private final ModelMapper modelMapper;
	private final LoanRepository loanRepository;

	public BookServiceImpl(BookRepository bookRepository, AuthorRepository authorRepository,
						   CategoryRepository categoryRepository, ModelMapper modelMapper, LoanRepository loanRepository) {
		this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
		this.loanRepository = loanRepository;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	@Cacheable(value = "books", key = "#pageRequest.pageNumber + '-' + #pageRequest.pageSize")
	public BookPageResponse findAllBooks(GeneralPageRequest pageRequest) {
		int pageNumber = pageRequest.getPageNumber() < 1 ? 0 : pageRequest.getPageNumber() - 1; // Adjust for zero-based index
		int pageSize = pageRequest.getPageSize() < 1 ? 5 : pageRequest.getPageSize();

		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<Book> page = bookRepository.findAll(pageable);

		List<BookDto> books = page.getContent().stream()
				.map(book -> {
					BookDto dto = convertToDto(book);
					Long borrowCount = bookRepository.findBorrowCountByBookId(book.getId()); // Assuming this method exists
					dto.setBorrowCount(borrowCount); // Set borrow count in DTO
					return dto;
				})
				.collect(Collectors.toList());

		return new BookPageResponse(books, pageNumber, page.getTotalElements(), page.getTotalPages()); // Adjust pageNumber for client-side
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	@Cacheable(value = "books", key = "#request.pageNumber + '-' + #request.pageSize + '-' + 'filter'")
	public BookPageResponse getBookListByPage(BookPageRequest request) {
		int pageNumber = request.getPageNumber() < 1 ? 0 : request.getPageNumber() - 1;
		int pageSize = request.getPageSize() < 1 ? 5 : request.getPageSize();

		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<Book> page = bookRepository.findBooksByCriteria(request, pageable);

		List<BookDto> books = page.getContent().stream()
				.map(book -> {
					BookDto dto = convertToDto(book);
					Long borrowCount = bookRepository.findBorrowCountByBookId(book.getId()); // Assuming this method exists
					dto.setBorrowCount(borrowCount); // Set borrow count in DTO
					return dto;
				})
				.collect(Collectors.toList());

		return new BookPageResponse(books, pageNumber, page.getTotalElements(), page.getTotalPages());
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	@Cacheable(value = "books", key = "#id")
	public BookDto findBookById(Long id) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(String.format("Book not found with ID %d", id)));
		return convertToDto(book);
	}

	@Transactional
	@Override
	public String createBook(BookDto bookDto) {
		Book book = convertToEntity(bookDto);
		// Ensure the book does not already exist by checking its ID (should be null for new books)
		if (book.getId() != null && bookRepository.existsById(book.getId())) {
			throw new IllegalArgumentException("Book with this ID already exists.");
		}
		bookRepository.save(book);
		return "SUCCESS";
	}

	@Transactional
	@Override
	@CachePut(value = "books", key = "#bookDto.id")
	public String updateBook(BookDto bookDto) {
		Book book = convertToEntity(bookDto);
		if (!bookRepository.existsById(book.getId())) {
			throw new NotFoundException(String.format("Book not found with ID %d", book.getId()));
		}
		bookRepository.save(book);
		return "SUCCESS";
	}

	@Transactional
	@Override
	@CacheEvict(value = "books", key = "#id")
	public void deleteBook(Long id) {
		if (!bookRepository.existsById(id)) {
			throw new NotFoundException(String.format("Book not found with ID %d", id));
		}
		bookRepository.deleteById(id);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	@Cacheable(value = "books", key = "#pageRequest.pageNumber + '-' + #pageRequest.pageSize + '-mostBorrowed'")
	public CustomPageResponse<BookDto> getMostBorrowedBooks(GeneralPageRequest pageRequest) {
		int pageNumber = pageRequest.getPageNumber() < 1 ? 0 : pageRequest.getPageNumber() - 1;
		int pageSize = pageRequest.getPageSize() < 1 ? 5 : pageRequest.getPageSize();

		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<Object[]> results = bookRepository.findMostBorrowedBooks(LoanStatus.ACTIVE, pageable);

		List<BookDto> bookDtos = results.getContent().stream()
				.map(result -> {
					Book book = (Book) result[0];
					Long borrowCount = (Long) result[1];
					BookDto bookDto = convertToDto(book);
					bookDto.setBorrowCount(borrowCount); // Assuming BookDto has a borrowCount field
					return bookDto;
				})
				.collect(Collectors.toList());

		return
				new CustomPageResponse<>(bookDtos, (int) results.getTotalElements(), results.getTotalPages(), results.getNumber());
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	@Cacheable(value = "books", key = "#pageRequest.pageNumber + '-' + #pageRequest.pageSize + '-runningLow-' + #threshold")
	public CustomPageResponse<BookDto> findBooksRunningLow(int threshold, GeneralPageRequest pageRequest) {
		int pageNumber = pageRequest.getPageNumber() < 1 ? 0 : pageRequest.getPageNumber() - 1;
		int pageSize = pageRequest.getPageSize() < 1 ? 5 : pageRequest.getPageSize();

		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<Object[]> results = bookRepository.findBooksRunningLowWithBorrowCount(threshold, pageable);

		List<BookDto> bookDtos = results.getContent().stream()
				.map(result -> {
					Book book = (Book) result[0]; // Book entity
					Long borrowCount = (Long) result[1]; // Borrow count
					BookDto bookDto = convertToDto(book);
					bookDto.setBorrowCount(borrowCount); // Set borrow count in DTO
					return bookDto;
				})
				.collect(Collectors.toList());

		return new CustomPageResponse<>(bookDtos, (int) results.getTotalElements(), results.getTotalPages(), results.getNumber());
	}

	private BookDto convertToDto(Book book) {
		BookDto bookDto = modelMapper.map(book, BookDto.class);
		bookDto.setAuthorIds(book.getAuthors().stream()
				.map(Author::getId)
				.collect(Collectors.toSet()));
		bookDto.setCategoryIds(book.getCategories().stream()
				.map(Category::getId)
				.collect(Collectors.toSet()));
		return bookDto;
	}

	private Book convertToEntity(BookDto bookDto) {
		Book book = modelMapper.map(bookDto, Book.class);

		// Manually set authors and categories based on IDs
		Set<Author> authors = bookDto.getAuthorIds().stream()
				.map(authorId -> authorRepository.findById(authorId)
						.orElseThrow(() -> new NotFoundException("Author not found")))
				.collect(Collectors.toSet());

		Set<Category> categories = bookDto.getCategoryIds().stream()
				.map(categoryId -> categoryRepository.findById(categoryId)
						.orElseThrow(() -> new NotFoundException("Category not found")))
				.collect(Collectors.toSet());

		book.setAuthors(authors);
		book.setCategories(categories);

		return book;
	}
}


