package com.librarybook.management.service;

import com.librarybook.management.entity.Book;
import com.librarybook.management.entity.BorrowRecord;
import com.librarybook.management.entity.BorrowStatus;
import com.librarybook.management.entity.Role;
import com.librarybook.management.entity.User;
import com.librarybook.management.repository.BookRepository;
import com.librarybook.management.repository.BorrowRecordRepository;
import com.librarybook.management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;

    public DashboardService(BookRepository bookRepository, BorrowRecordRepository borrowRecordRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getStudentDashboard(User user) {
        Map<String, Object> dashboard = new HashMap<>();
        long activeLoans = borrowRecordRepository.countByUserAndStatus(user, BorrowStatus.ACTIVE);
        long overdue = borrowRecordRepository.countByUserAndStatusAndDueDateBefore(user, BorrowStatus.ACTIVE, java.time.LocalDate.now());
        dashboard.put("totalBooks", bookRepository.countByActiveTrue());
        dashboard.put("activeLoans", activeLoans);
        dashboard.put("overdue", overdue);
        dashboard.put("historyCount", borrowRecordRepository.countByUser(user));
        return dashboard;
    }

    public Map<String, Object> getLibrarianDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        List<Book> books = bookRepository.findByActiveTrue();
        int availableCopies = books.stream().mapToInt(Book::getAvailableCopies).sum();
        long activeLoans = borrowRecordRepository.countByStatus(BorrowStatus.ACTIVE);
        long overdue = borrowRecordRepository.countByStatusAndDueDateBefore(BorrowStatus.ACTIVE, java.time.LocalDate.now());
        dashboard.put("totalBooks", books.size());
        dashboard.put("availableCopies", availableCopies);
        dashboard.put("issuedBooks", activeLoans);
        dashboard.put("overdueRecords", overdue);
        dashboard.put("totalUsers", userRepository.count());
        return dashboard;
    }

    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.putAll(getLibrarianDashboard());
        long librarians = userRepository.countByRole(Role.LIBRARIAN);
        long students = userRepository.countByRole(Role.STUDENT);
        dashboard.put("librarians", librarians);
        dashboard.put("students", students);
        return dashboard;
    }
}
