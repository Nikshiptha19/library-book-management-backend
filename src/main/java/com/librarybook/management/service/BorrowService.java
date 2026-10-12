package com.librarybook.management.service;

import com.librarybook.management.entity.BorrowRecord;
import com.librarybook.management.entity.BorrowStatus;
import com.librarybook.management.entity.Book;
import com.librarybook.management.entity.Role;
import com.librarybook.management.entity.User;
import com.librarybook.management.exception.BusinessException;
import com.librarybook.management.exception.ResourceNotFoundException;
import com.librarybook.management.repository.BookRepository;
import com.librarybook.management.repository.BorrowRecordRepository;
import com.librarybook.management.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowService {
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;

    public BorrowService(BookRepository bookRepository, BorrowRecordRepository borrowRecordRepository,
                         UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BorrowRecord issueBook(User user, Long bookId) {
        if (user == null || user.getId() == null) {
            throw new BusinessException("User is not active");
        }
        User lockedUser = userRepository.findByIdForUpdate(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!lockedUser.isActive()) {
            throw new BusinessException("User is not active");
        }
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        if (!book.isActive()) {
            throw new BusinessException("Book is inactive");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessException("No copies available for this book");
        }
        if (borrowRecordRepository.countByUserAndStatus(lockedUser, BorrowStatus.ACTIVE) >= 5) {
            throw new BusinessException("Borrowing limit reached");
        }
        if (borrowRecordRepository.existsByUserAndBookAndStatus(lockedUser, book, BorrowStatus.ACTIVE)) {
            throw new BusinessException("You already have an active loan for this book");
        }

        BorrowRecord record = new BorrowRecord();
        record.setUser(lockedUser);
        record.setBook(book);
        record.setIssueDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(14));
        record.setStatus(BorrowStatus.ACTIVE);
        record.setIssuedBy(lockedUser);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        record = borrowRecordRepository.save(record);
        bookRepository.save(book);
        return record;
    }

    @Transactional
    public BorrowRecord returnBook(Long recordId, User currentUser) {
        BorrowRecord record = borrowRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));
        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BusinessException("Book already returned");
        }

        Book book = record.getBook();
        if (book == null) {
            throw new ResourceNotFoundException("Book not found for borrow record");
        }
        if (currentUser.getRole() == Role.STUDENT
                && !record.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot return another user's book");
        }
        book = bookRepository.findByIdForUpdate(book.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found for borrow record"));
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new BusinessException("Book inventory is inconsistent; contact a librarian");
        }

        record.setReturnDate(LocalDate.now());
        record.setReturnedBy(currentUser);
        record.setStatus(BorrowStatus.RETURNED);
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        borrowRecordRepository.save(record);
        bookRepository.save(book);
        return record;
    }

    public List<BorrowRecord> getMyBorrowRecords(User user) {
        return borrowRecordRepository.findByUserOrderByIssueDateDesc(user);
    }

    public List<BorrowRecord> getActiveLoans() {
        return borrowRecordRepository.findByStatusOrderByDueDateAsc(BorrowStatus.ACTIVE);
    }

    public List<BorrowRecord> getOverdueRecords() {
        return borrowRecordRepository.findByStatusAndDueDateBeforeOrderByDueDateAsc(BorrowStatus.ACTIVE, LocalDate.now());
    }
}
