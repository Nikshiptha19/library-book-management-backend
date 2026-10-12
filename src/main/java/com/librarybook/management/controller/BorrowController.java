package com.librarybook.management.controller;

import com.librarybook.management.dto.ApiResponse;
import com.librarybook.management.dto.BorrowRequest;
import com.librarybook.management.entity.BorrowRecord;
import com.librarybook.management.entity.User;
import com.librarybook.management.service.BorrowService;
import com.librarybook.management.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BorrowController {
    private final BorrowService borrowService;
    private final UserService userService;

    public BorrowController(BorrowService borrowService, UserService userService) {
        this.borrowService = borrowService;
        this.userService = userService;
    }

    @PostMapping("/borrow")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> issueBook(@RequestBody BorrowRequest request) {
        User currentUser = userService.getCurrentUser();
        BorrowRecord record = borrowService.issueBook(currentUser, request.getBookId());
        return ResponseEntity.ok(new ApiResponse(true, "Book issued successfully", record));
    }

    @GetMapping("/borrow/my-records")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getMyBorrowRecords() {
        User currentUser = userService.getCurrentUser();
        List<BorrowRecord> records = borrowService.getMyBorrowRecords(currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Your borrow records", records));
    }

    @GetMapping("/borrow/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getHistory() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(new ApiResponse(true, "Borrow history retrieved", borrowService.getMyBorrowRecords(currentUser)));
    }

    @GetMapping("/borrow/active")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<ApiResponse> getActiveLoans() {
        return ResponseEntity.ok(new ApiResponse(true, "Active loans retrieved", borrowService.getActiveLoans()));
    }

    @GetMapping("/borrow/overdue")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<ApiResponse> getOverdue() {
        return ResponseEntity.ok(new ApiResponse(true, "Overdue records retrieved", borrowService.getOverdueRecords()));
    }

    @PostMapping("/borrow/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','STUDENT')")
    public ResponseEntity<ApiResponse> returnBook(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        BorrowRecord record = borrowService.returnBook(id, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Book returned successfully", record));
    }
}
