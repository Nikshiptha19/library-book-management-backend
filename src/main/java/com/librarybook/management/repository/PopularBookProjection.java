package com.librarybook.management.repository;

public interface PopularBookProjection {
    Long getBookId();
    String getTitle();
    Long getBorrowCount();
}
