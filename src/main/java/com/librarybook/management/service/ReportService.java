package com.librarybook.management.service;

import com.librarybook.management.entity.BorrowRecord;
import com.librarybook.management.entity.BorrowStatus;
import com.librarybook.management.repository.BorrowRecordRepository;
import com.librarybook.management.repository.PopularBookProjection;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final BorrowRecordRepository borrowRecordRepository;

    public ReportService(BorrowRecordRepository borrowRecordRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
    }

    public Map<String, Object> getBorrowingSummary() {
        long active = borrowRecordRepository.countByStatus(BorrowStatus.ACTIVE);
        long returned = borrowRecordRepository.countByStatus(BorrowStatus.RETURNED);
        Map<String, Object> response = new HashMap<>();
        response.put("activeLoans", active);
        response.put("returnedBooks", returned);
        response.put("totalRecords", borrowRecordRepository.count());
        return response;
    }

    public List<Map<String, Object>> getPopularBooks() {
        List<PopularBookProjection> popularBooks = borrowRecordRepository.findPopularBooks(PageRequest.of(0, 5));
        return popularBooks.stream().map(book -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("bookId", book.getBookId());
                    item.put("title", book.getTitle());
                    item.put("borrowCount", book.getBorrowCount());
                    return item;
                })
                .toList();
    }
}
