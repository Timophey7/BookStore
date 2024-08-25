package com.library.service.impl;

import com.library.exceptions.BookNotFoundException;
import com.library.models.Book;
import com.library.models.BookDTO;
import com.library.models.BookFormat;
import com.library.models.BookResponse;
import com.library.repository.BookRepository;
import com.library.service.BookService;
import com.library.service.utils.BookUniqueCodeGenerator;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@EnableTransactionManagement
public class BookServiceImpl implements BookService {

    ModelMapper modelMapper;

    BookRepository bookRepository;

    BookUniqueCodeGenerator bookUniqueCodeGenerator;

    EntityManager entityManager;


    @Override
    @Cacheable(value = "books", key = "#pageNum")
    public List<BookResponse> getAllBooks(int pageNum,int value) {
        return bookRepository.getAllBooks(pageNum*value,value)
                .stream().map(this::mapToBookResponse).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "booksAuthor", key = "#pageNum")
    public List<BookResponse> getAllBooksByAuthor(String author,int pageNum,int value) {
        return bookRepository.getAllBooksByAuthor(author,pageNum*value, value)
                .stream().map(this::mapToBookResponse).collect(Collectors.toList());
    }

    @Override
    public boolean bookExists(String title, BookFormat format) {
        return bookRepository.findBookByTitleAndBookFormat(title, format) != null;
    }

    @Override
    @Cacheable(value = "bookByUniqueCode", key = "#uniqueCode")
    public BookResponse getBookByUniqueCode(String uniqueCode) throws BookNotFoundException {
        Book bookNotFound = bookRepository.findBookByUniqueCode(uniqueCode)
                .orElseThrow(() -> new BookNotFoundException("book not found"));
        return mapToBookResponse(bookNotFound);
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "books",allEntries = true),
            @CacheEvict(value = "booksAuthor",allEntries = true)
            }
    )
    public void deleteBookByUniqueCode(String uniqueCode) throws BookNotFoundException {
        Book bookNotFound = bookRepository.findBookByUniqueCode(uniqueCode)
                .orElseThrow(() -> new BookNotFoundException("book not found"));
        entityManager.remove(bookNotFound);
    }

    @Override
    public BookResponse mapToBookResponse(Book book) {
        return modelMapper.map(book, BookResponse.class);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "books",allEntries = true),
            @CacheEvict(value = "booksAuthor",allEntries = true)
    }
    )
    public void saveBook(BookDTO bookDTO) {
        bookDTO.setUniqueCode(bookUniqueCodeGenerator.generateBookUniqueCode(bookDTO.getTitle()));
        Book book = modelMapper.map(bookDTO, Book.class);
        bookRepository.save(book);
    }



}
