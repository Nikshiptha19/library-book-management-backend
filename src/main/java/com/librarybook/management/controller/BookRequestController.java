package com.librarybook.management.controller;

import com.librarybook.management.dto.ApiResponse;
import com.librarybook.management.entity.BookRequest;
import com.librarybook.management.entity.User;
import com.librarybook.management.service.BookRequestService;
import com.librarybook.management.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookRequestController {
    private final BookRequestService bookRequestService;
    private final UserService userService;

    public BookRequestController(BookRequestService bookRequestService, UserService userService) {
        this.bookRequestService = bookRequestService;
        this.userService = userService;
    }

    @PostMapping("/requests")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> createRequest(@RequestParam Long bookId) {
        User currentUser = userService.getCurrentUser();
        BookRequest request = bookRequestService.createRequest(currentUser, bookId);
        return ResponseEntity.ok(new ApiResponse(true, "Book request created", request));
    }

    @GetMapping("/requests/my-requests")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> getMyRequests() {
        User currentUser = userService.getCurrentUser();
        List<BookRequest> requests = bookRequestService.getMyRequests(currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Your requests", requests));
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<ApiResponse> getAllRequests() {
        return ResponseEntity.ok(new ApiResponse(true, "Requests retrieved", bookRequestService.getAllRequests()));
    }

    @PutMapping("/requests/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<ApiResponse> approveRequest(@PathVariable Long id) {
        User reviewer = userService.getCurrentUser();
        BookRequest request = bookRequestService.approveRequest(id, reviewer);
        return ResponseEntity.ok(new ApiResponse(true, "Request approved", request));
    }

    @PutMapping("/requests/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<ApiResponse> rejectRequest(@PathVariable Long id) {
        User reviewer = userService.getCurrentUser();
        BookRequest request = bookRequestService.rejectRequest(id, reviewer);
        return ResponseEntity.ok(new ApiResponse(true, "Request rejected", request));
    }

    @DeleteMapping("/requests/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> cancelRequest(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        bookRequestService.cancelRequest(id, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Request cancelled", null));
    }
}
