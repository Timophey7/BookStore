package com.library.service;

import com.library.exceptions.BookNotFoundException;
import com.library.models.Book;
import com.library.models.BookDTO;
import com.library.models.BookFormat;
import com.library.models.BookResponse;

import java.util.List;

public interface BookService {
    List<BookResponse> getAllBooks(int pageNum,int value);

    List<BookResponse> getAllBooksByAuthor(String author,int pageNum,int value);

    boolean bookExists(String title, BookFormat format);

    BookResponse getBookByUniqueCode(String uniqueCode) throws BookNotFoundException;

    void deleteBookByUniqueCode(String uniqueCode) throws BookNotFoundException;

    BookResponse mapToBookResponse(Book book);

    void saveBook(BookDTO book);
}
