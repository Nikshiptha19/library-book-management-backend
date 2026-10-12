package com.librarybook.management.repository;

import com.librarybook.management.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT b FROM Book b JOIN b.category c WHERE b.active = true AND " +
           "(:keyword is null or lower(b.title) like lower(concat('%', :keyword, '%')) or lower(b.author) like lower(concat('%', :keyword, '%')) or lower(b.isbn) like lower(concat('%', :keyword, '%'))) " +
           "AND (:categoryId is null or c.id = :categoryId) " +
           "AND (:availableOnly = false or b.availableCopies > 0)")
    Page<Book> searchBooks(@Param("keyword") String keyword,
                           @Param("categoryId") Long categoryId,
                           @Param("availableOnly") boolean availableOnly,
                           Pageable pageable);

    boolean existsByIsbn(String isbn);
    boolean existsByCategoryId(Long categoryId);
    long countByActiveTrue();
    List<Book> findByActiveTrue();
}
