package com.librarybook.management.service;

import com.librarybook.management.entity.Book;
import com.librarybook.management.entity.BookRequest;
import com.librarybook.management.entity.BookRequestStatus;
import com.librarybook.management.entity.User;
import com.librarybook.management.exception.BusinessException;
import com.librarybook.management.exception.ResourceNotFoundException;
import com.librarybook.management.repository.BookRepository;
import com.librarybook.management.repository.BookRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookRequestService {
    private final BookRequestRepository bookRequestRepository;
    private final BookRepository bookRepository;

    public BookRequestService(BookRequestRepository bookRequestRepository, BookRepository bookRepository) {
        this.bookRequestRepository = bookRequestRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public BookRequest createRequest(User user, Long bookId) {
        if (user == null || !user.isActive()) {
            throw new BusinessException("User is not active");
        }
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        if (!book.isActive()) {
            throw new BusinessException("Book is inactive");
        }
        if (bookRequestRepository.existsByUserAndBookAndStatus(user, book, BookRequestStatus.PENDING)) {
            throw new BusinessException("You already have a pending request for this book");
        }

        BookRequest request = new BookRequest();
        request.setUser(user);
        request.setBook(book);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(BookRequestStatus.PENDING);
        return bookRequestRepository.save(request);
    }

    public List<BookRequest> getMyRequests(User user) {
        return bookRequestRepository.findByUserOrderByRequestDateDesc(user);
    }

    public List<BookRequest> getAllRequests() {
        return bookRequestRepository.findAllByOrderByRequestDateDesc();
    }

    @Transactional
    public BookRequest approveRequest(Long id, User reviewedBy) {
        BookRequest request = bookRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        ensurePending(request);
        request.setStatus(BookRequestStatus.APPROVED);
        request.setReviewedBy(reviewedBy);
        return bookRequestRepository.save(request);
    }

    @Transactional
    public BookRequest rejectRequest(Long id, User reviewedBy) {
        BookRequest request = bookRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        ensurePending(request);
        request.setStatus(BookRequestStatus.REJECTED);
        request.setReviewedBy(reviewedBy);
        return bookRequestRepository.save(request);
    }

    @Transactional
    public void cancelRequest(Long id, User currentUser) {
        BookRequest request = bookRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        if (!request.getUser().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot cancel another user's request");
        }
        ensurePending(request);
        request.setStatus(BookRequestStatus.CANCELLED);
        bookRequestRepository.save(request);
    }

    private void ensurePending(BookRequest request) {
        if (request.getStatus() != BookRequestStatus.PENDING) {
            throw new BusinessException("Only pending requests can be reviewed or cancelled");
        }
    }
}
