package com.librarybook.management.repository;

import com.librarybook.management.entity.BookRequest;
import com.librarybook.management.entity.Book;
import com.librarybook.management.entity.BookRequestStatus;
import com.librarybook.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface BookRequestRepository extends JpaRepository<BookRequest, Long> {
    List<BookRequest> findByUserOrderByRequestDateDesc(User user);
    List<BookRequest> findAllByOrderByRequestDateDesc();
    boolean existsByUserAndBookAndStatus(User user, Book book, BookRequestStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BookRequest r where r.id = :id")
    Optional<BookRequest> findByIdForUpdate(@Param("id") Long id);
}
