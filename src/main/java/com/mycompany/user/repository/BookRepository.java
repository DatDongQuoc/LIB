package com.mycompany.user.repository;

import com.mycompany.user.dto.request.BookPageRequest;
import com.mycompany.user.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.mycompany.user.entity.Book;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long>{

    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN b.authors a " +
            "LEFT JOIN b.categories c " +
            "WHERE (:#{#request.id} IS NULL OR b.id = :#{#request.id}) " +
            "AND (:#{#request.isbn} IS NULL OR b.isbn LIKE %:#{#request.isbn}%) " +
            "AND (:#{#request.name} IS NULL OR b.name LIKE %:#{#request.name}%) " +
            "AND (:#{#request.serialName} IS NULL OR b.serialName LIKE %:#{#request.serialName}%) " +
            "AND (:#{#request.description} IS NULL OR b.description LIKE %:#{#request.description}%) " +
            "AND (:#{#request.quantity} IS NULL OR b.quantity = :#{#request.quantity}) " +
            "AND (:#{#request.authorIds} IS NULL OR a.id IN :#{#request.authorIds}) " +
            "AND (:#{#request.categoryIds} IS NULL OR c.id IN :#{#request.categoryIds})")
    Page<Book> findBooksByCriteria(@Param("request") BookPageRequest request, Pageable pageable);

    @Query("SELECT b, COALESCE(SUM(ld.quantity), 0) AS borrowCount " +
            "FROM Loan l JOIN l.loanDetails ld JOIN ld.book b " +
            "WHERE l.status = :status " +
            "GROUP BY b.id " +
            "ORDER BY borrowCount DESC")
    Page<Object[]> findMostBorrowedBooks(@Param("status") LoanStatus status, Pageable pageable);

    @Query("SELECT b, COALESCE(SUM(ld.quantity), 0) AS borrowCount " +
            "FROM Book b LEFT JOIN LoanDetail ld ON ld.book.id = b.id " +
            "WHERE b.quantity <= :threshold " +
            "GROUP BY b.id " +
            "ORDER BY borrowCount DESC")
    Page<Object[]> findBooksRunningLowWithBorrowCount(@Param("threshold") int threshold, Pageable pageable);

    @Query("SELECT COALESCE(SUM(ld.quantity), 0) FROM Loan l JOIN l.loanDetails ld WHERE ld.book.id = :bookId")
    Long findBorrowCountByBookId(@Param("bookId") Long bookId);
}
