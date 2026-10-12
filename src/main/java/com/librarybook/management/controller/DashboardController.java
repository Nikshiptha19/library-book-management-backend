package com.librarybook.management.controller;

import com.librarybook.management.dto.ApiResponse;
import com.librarybook.management.entity.User;
import com.librarybook.management.service.DashboardService;
import com.librarybook.management.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;
    private final UserService userService;

    public DashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> studentDashboard() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(new ApiResponse(true, "Student dashboard", dashboardService.getStudentDashboard(currentUser)));
    }

    @GetMapping("/librarian")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<ApiResponse> librarianDashboard() {
        return ResponseEntity.ok(new ApiResponse(true, "Librarian dashboard", dashboardService.getLibrarianDashboard()));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> adminDashboard() {
        return ResponseEntity.ok(new ApiResponse(true, "Admin dashboard", dashboardService.getAdminDashboard()));
    }
}
