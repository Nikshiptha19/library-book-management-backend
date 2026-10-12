package com.librarybook.management.service;

import com.librarybook.management.entity.Book;
import com.librarybook.management.entity.Category;
import com.librarybook.management.entity.User;
import com.librarybook.management.exception.BusinessException;
import com.librarybook.management.exception.DuplicateResourceException;
import com.librarybook.management.exception.ResourceNotFoundException;
import com.librarybook.management.repository.BookRepository;
import com.librarybook.management.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class BookService {
    private static final Set<String> SORTABLE_FIELDS = Set.of("title", "author", "publicationYear", "isbn", "id");

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<Book> getBooks(String keyword, Long categoryId, Boolean availableOnly, int page, int size, String sortBy, String direction) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException("Page must be non-negative and size must be between 1 and 100");
        }
        String sortField = sortBy == null || sortBy.isBlank() ? "title" : sortBy;
        if (!SORTABLE_FIELDS.contains(sortField)) {
            throw new BusinessException("Unsupported book sort field");
        }
        Sort sort = direction != null && direction.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return bookRepository.searchBooks(keyword, categoryId, availableOnly != null && availableOnly, pageable);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    public Book createBook(Book book) {
        if (book.getCategory() == null || book.getCategory().getId() == null) {
            throw new BusinessException("Category is required");
        }
        Category category = categoryRepository.findById(book.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        book.setCategory(category);
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new DuplicateResourceException("ISBN already exists");
        }
        book.setAvailableCopies(book.getTotalCopies());
        book.setActive(true);
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, Book updated) {
        Book existing = getBookById(id);
        if (updated.getCategory() != null && updated.getCategory().getId() != null) {
            Category category = categoryRepository.findById(updated.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            existing.setCategory(category);
        }
        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getAuthor() != null) existing.setAuthor(updated.getAuthor());
        if (updated.getIsbn() != null && !updated.getIsbn().equals(existing.getIsbn()) && bookRepository.existsByIsbn(updated.getIsbn())) {
            throw new DuplicateResourceException("ISBN already exists");
        }
        if (updated.getIsbn() != null) existing.setIsbn(updated.getIsbn());
        if (updated.getPublisher() != null) existing.setPublisher(updated.getPublisher());
        if (updated.getPublicationYear() != null) existing.setPublicationYear(updated.getPublicationYear());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getTotalCopies() != null) {
            int issuedCopies = existing.getTotalCopies() - existing.getAvailableCopies();
            if (updated.getTotalCopies() < issuedCopies) {
                throw new BusinessException("Total copies cannot be less than the number of copies currently on loan");
            }
            existing.setTotalCopies(updated.getTotalCopies());
            existing.setAvailableCopies(updated.getTotalCopies() - issuedCopies);
        }
        return bookRepository.save(existing);
    }

    public void deleteBook(Long id) {
        Book book = getBookById(id);
        book.setActive(false);
        bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
}
