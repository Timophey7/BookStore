package com.library.service.impl;

import com.library.exceptions.BookNotFoundException;
import com.library.models.Book;
import com.library.models.BookDTO;
import com.library.models.BookFormat;
import com.library.models.BookResponse;
import com.library.repository.BookRepository;
import com.library.service.utils.BookUniqueCodeGenerator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @InjectMocks
    BookServiceImpl bookService;

    @Mock
    ModelMapper modelMapper;
    @Mock
    BookRepository bookRepository;
    @Mock
    BookUniqueCodeGenerator bookUniqueCodeGenerator;
    @Mock
    EntityManager entityManager;

    BookResponse bookResponse;
    Book book = new Book();

    private final static String UNIQUE_CODE = "werty123";

    @BeforeEach
    void setUp() {
        bookResponse = new BookResponse();
        bookResponse.setUniqueCode(UNIQUE_CODE);
        bookResponse.setAuthor("TestAuthor");
        bookResponse.setPrice(1000);
        bookResponse.setTitle("test");
        book = new Book();
        book.setUniqueCode(UNIQUE_CODE);
        book.setAuthor("TestAuthor");
        book.setPrice(1000);
        book.setTitle("test");
    }

    @Test
    void getAllBooks() {
        when(bookRepository.getAllBooks(0,10)).thenReturn(List.of(book));
        when(modelMapper.map(book,BookResponse.class)).thenReturn(bookResponse);

        List<BookResponse> allBooks = bookService.getAllBooks(0,10);

        assertFalse(allBooks.isEmpty());
        assertEquals(allBooks.get(0).getAuthor(),book.getAuthor());
    }

    @Test
    void getAllBooksByAuthor() {
        String author = "TestAuthor";
        when(bookRepository.getAllBooksByAuthor(author,0,10)).thenReturn(List.of(book));
        when(modelMapper.map(book,BookResponse.class)).thenReturn(bookResponse);

        List<BookResponse> allBooksByAuthor = bookService.getAllBooksByAuthor(author,0,10);

        assertFalse(allBooksByAuthor.isEmpty());
        assertEquals(allBooksByAuthor.get(0).getAuthor(),book.getAuthor());
    }

    @Test
    void bookExistsShouldReturnTrue() {
        when(bookRepository.findBookByTitleAndBookFormat(book.getTitle(), BookFormat.BOOK)).thenReturn(book);

        boolean bookExists = bookService.bookExists(book.getTitle(), BookFormat.BOOK);

        assertTrue(bookExists);
    }

    @Test
    void bookExistsShouldReturnFalse() {
        when(bookRepository.findBookByTitleAndBookFormat(book.getTitle(), BookFormat.BOOK)).thenReturn(null);

        boolean bookExists = bookService.bookExists(book.getTitle(), BookFormat.BOOK);

        assertFalse(bookExists);
    }

    @Test
    void getBookByUniqueCodeShouldReturnBookResponse() throws BookNotFoundException {
        when(bookRepository.findBookByUniqueCode(UNIQUE_CODE)).thenReturn(Optional.of(book));
        when(modelMapper.map(book,BookResponse.class)).thenReturn(bookResponse);

        BookResponse bookByUniqueCode = bookService.getBookByUniqueCode(UNIQUE_CODE);

        assertEquals(bookByUniqueCode.getAuthor(),book.getAuthor());
        verify(bookRepository,times(1)).findBookByUniqueCode(UNIQUE_CODE);

    }

    @Test
    void getBookByUniqueCodeShouldThrowBookNotFoundException() throws BookNotFoundException {
        when(bookRepository.findBookByUniqueCode(UNIQUE_CODE)).thenReturn(Optional.empty());

        BookNotFoundException bookNotFoundException = assertThrows(BookNotFoundException.class, () -> {
            bookService.getBookByUniqueCode(UNIQUE_CODE);
        });

        assertEquals(bookNotFoundException.getMessage(),"book not found");
        verify(bookRepository,times(1)).findBookByUniqueCode(UNIQUE_CODE);
    }

    @Test
    void deleteBookByUniqueCodeShouldSuccessfullyDelete() throws BookNotFoundException {
        when(bookRepository.findBookByUniqueCode(UNIQUE_CODE)).thenReturn(Optional.of(book));
        doNothing().when(entityManager).remove(book);

        bookService.deleteBookByUniqueCode(UNIQUE_CODE);

        verify(bookRepository,times(1)).findBookByUniqueCode(UNIQUE_CODE);
        verify(entityManager,times(1)).remove(book);
    }

    @Test
    void deleteBookByUniqueCodeShouldThrowsBookNotFoundException() throws BookNotFoundException {
        when(bookRepository.findBookByUniqueCode(UNIQUE_CODE)).thenReturn(Optional.empty());

        BookNotFoundException bookNotFoundException = assertThrows(BookNotFoundException.class, () -> {
            bookService.deleteBookByUniqueCode(UNIQUE_CODE);
        });

        assertEquals(bookNotFoundException.getMessage(),"book not found");
        verify(bookRepository,times(1)).findBookByUniqueCode(UNIQUE_CODE);
    }

    @Test
    void mapToBookResponse() {
        when(modelMapper.map(book,BookResponse.class)).thenReturn(bookResponse);

        BookResponse response = bookService.mapToBookResponse(book);

        assertNotNull(response);
        assertEquals(response,bookResponse);
    }


}