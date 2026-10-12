package com.librarybook.management.controller;

import com.librarybook.management.dto.ApiResponse;
import com.librarybook.management.entity.Book;
import com.librarybook.management.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public ResponseEntity<ApiResponse> getBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "false") Boolean availableOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Page<Book> bookPage = bookService.getBooks(keyword, categoryId, availableOnly, page, size, sortBy, direction);
        return ResponseEntity.ok(new ApiResponse(true, "Books retrieved", bookPage.getContent()));
    }

    @GetMapping("/books/search")
    public ResponseEntity<ApiResponse> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "false") Boolean availableOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Book> bookPage = bookService.getBooks(keyword, categoryId, availableOnly, page, size, "title", "asc");
        return ResponseEntity.ok(new ApiResponse(true, "Search results", bookPage.getContent()));
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<ApiResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse(true, "Book retrieved", bookService.getBookById(id)));
    }

    @PostMapping("/books")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<ApiResponse> createBook(@Valid @RequestBody Book book) {
        return ResponseEntity.ok(new ApiResponse(true, "Book created", bookService.createBook(book)));
    }

    @PutMapping("/books/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<ApiResponse> updateBook(@PathVariable Long id, @Valid @RequestBody Book updatedBook) {
        return ResponseEntity.ok(new ApiResponse(true, "Book updated", bookService.updateBook(id, updatedBook)));
    }

    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<ApiResponse> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(new ApiResponse(true, "Book disabled", null));
    }
}
