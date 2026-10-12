package com.librarybook.management.controller;

import com.librarybook.management.dto.ApiResponse;
import com.librarybook.management.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/borrowing-summary")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<ApiResponse> borrowingSummary() {
        return ResponseEntity.ok(new ApiResponse(true, "Borrowing summary", reportService.getBorrowingSummary()));
    }

    @GetMapping("/popular-books")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<ApiResponse> popularBooks() {
        return ResponseEntity.ok(new ApiResponse(true, "Popular books", reportService.getPopularBooks()));
    }
}
