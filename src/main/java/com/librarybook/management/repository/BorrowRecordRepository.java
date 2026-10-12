package com.librarybook.management.repository;

import com.librarybook.management.entity.BorrowRecord;
import com.librarybook.management.entity.BorrowStatus;
import com.librarybook.management.entity.Book;
import com.librarybook.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    long countByUserAndStatus(User user, BorrowStatus status);
    boolean existsByUserAndBookAndStatus(User user, Book book, BorrowStatus status);
    List<BorrowRecord> findByUserOrderByIssueDateDesc(User user);
    List<BorrowRecord> findByStatusOrderByDueDateAsc(BorrowStatus status);
    List<BorrowRecord> findByStatusAndDueDateBeforeOrderByDueDateAsc(BorrowStatus status, java.time.LocalDate date);
    long countByStatus(BorrowStatus status);
    long countByStatusAndDueDateBefore(BorrowStatus status, java.time.LocalDate date);
    long countByUser(User user);
    long countByUserAndStatusAndDueDateBefore(User user, BorrowStatus status, java.time.LocalDate date);

    @Query("select r.book.id as bookId, r.book.title as title, count(r) as borrowCount " +
            "from BorrowRecord r group by r.book.id, r.book.title order by count(r) desc")
    List<PopularBookProjection> findPopularBooks(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BorrowRecord r where r.id = :id")
    Optional<BorrowRecord> findByIdForUpdate(@Param("id") Long id);
}
