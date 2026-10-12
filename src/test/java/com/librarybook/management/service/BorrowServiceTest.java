package com.librarybook.management.service;

import com.librarybook.management.entity.Book;
import com.librarybook.management.entity.BorrowRecord;
import com.librarybook.management.entity.BorrowStatus;
import com.librarybook.management.entity.Role;
import com.librarybook.management.entity.User;
import com.librarybook.management.exception.BusinessException;
import com.librarybook.management.repository.BookRepository;
import com.librarybook.management.repository.BorrowRecordRepository;
import com.librarybook.management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BorrowRecordRepository borrowRecordRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BorrowService borrowService;

    @Test
    void allowsDifferentUsersToBorrowAvailableCopiesOfTheSameBook() {
        User user = user(10L);
        Book book = book(20L, 2, 2);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(bookRepository.findByIdForUpdate(book.getId())).thenReturn(Optional.of(book));
        when(borrowRecordRepository.countByUserAndStatus(user, BorrowStatus.ACTIVE)).thenReturn(0L);
        when(borrowRecordRepository.existsByUserAndBookAndStatus(user, book, BorrowStatus.ACTIVE)).thenReturn(false);
        when(borrowRecordRepository.save(any(BorrowRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> invocation.getArgument(0)).when(bookRepository).save(book);

        BorrowRecord issued = borrowService.issueBook(user, book.getId());

        assertEquals(BorrowStatus.ACTIVE, issued.getStatus());
        assertEquals(1, book.getAvailableCopies());
        verify(borrowRecordRepository).save(issued);
    }

    @Test
    void rejectsDuplicateActiveLoanByTheSameUser() {
        User user = user(10L);
        Book book = book(20L, 2, 1);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(bookRepository.findByIdForUpdate(book.getId())).thenReturn(Optional.of(book));
        when(borrowRecordRepository.countByUserAndStatus(user, BorrowStatus.ACTIVE)).thenReturn(1L);
        when(borrowRecordRepository.existsByUserAndBookAndStatus(user, book, BorrowStatus.ACTIVE)).thenReturn(true);

        assertThrows(BusinessException.class, () -> borrowService.issueBook(user, book.getId()));

        assertEquals(1, book.getAvailableCopies());
        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void preventsStudentFromReturningAnotherUsersLoan() {
        User borrower = user(10L);
        User otherStudent = user(11L);
        Book book = book(20L, 2, 1);
        BorrowRecord record = new BorrowRecord();
        record.setId(30L);
        record.setUser(borrower);
        record.setBook(book);
        record.setStatus(BorrowStatus.ACTIVE);
        when(borrowRecordRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(record));

        assertThrows(AccessDeniedException.class, () -> borrowService.returnBook(30L, otherStudent));

        assertEquals(1, book.getAvailableCopies());
        verify(bookRepository, never()).save(any());
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("student" + id + "@example.com");
        user.setRole(Role.STUDENT);
        user.setActive(true);
        return user;
    }

    private static Book book(Long id, int total, int available) {
        Book book = new Book();
        book.setId(id);
        book.setActive(true);
        book.setTotalCopies(total);
        book.setAvailableCopies(available);
        return book;
    }
}
