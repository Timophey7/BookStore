package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.exceptions.BookNotFoundException;
import com.library.http.headers.HeadersGenerator;
import com.library.models.Book;
import com.library.models.BookDTO;
import com.library.models.BookFormat;
import com.library.models.BookResponse;
import com.library.service.impl.BookServiceImpl;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    Bucket bucket;

    @MockBean
    BookServiceImpl bookService;

    @MockBean
    HeadersGenerator headersGenerator;

    ObjectMapper objectMapper;

    BookResponse bookResponse;

    Book book;

    private final static String uniqueCode = "werty123";

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setTitle("test");
        book.setBookFormat(BookFormat.BOOK);
        book.setId(1);
        objectMapper = new ObjectMapper();
        bookResponse = new BookResponse();
        bookResponse.setUniqueCode(uniqueCode);
        bookResponse.setPrice(1000);
        bookResponse.setAuthor("author");
        bookResponse.setPublisher("pub");
    }

    @Test
    void addBook_Created() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(true);
        when(bookService.bookExists(book.getTitle(),book.getBookFormat())).thenReturn(false);

        ResultActions perform = mockMvc.perform(post("http://localhost:8084/v1/library/books/addBook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book))
        );

        perform
                .andExpect(status().isCreated())
                .andExpect(content().string("successfully saved"));

    }

    @Test
    void addBook_BadRequest_BookAlreadyInCart() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(true);
        when(bookService.bookExists(book.getTitle(),book.getBookFormat())).thenReturn(true);

        ResultActions perform = mockMvc.perform(post("http://localhost:8084/v1/library/books/addBook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book))
        );

        perform.andExpect(status().isBadRequest());

    }

    @Test
    void addBook_ToManyRequests() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(false);

        ResultActions perform = mockMvc.perform(post("http://localhost:8084/v1/library/books/addBook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book))
        );

        perform.andExpect(status().isTooManyRequests());
    }

    @Test
    void getBookByUniqueCode_Ok() throws Exception {
        when(bookService.getBookByUniqueCode(uniqueCode)).thenReturn(bookResponse);

        ResultActions perform = mockMvc.perform(get("http://localhost:8084/v1/library/books/" + uniqueCode));

        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(1000));
    }

    @Test
    void getBookByUniqueCode_NotFound() throws Exception {
        when(bookService.getBookByUniqueCode("uniqueCode")).thenThrow(BookNotFoundException.class);

        ResultActions perform = mockMvc.perform(get("http://localhost:8084/v1/library/books/" + "uniqueCode"));

        perform.andExpect(status().isNotFound());
    }

    @Test
    void getBooks_Ok() throws Exception {
        when(bookService.getAllBooks(0,10)).thenReturn(List.of(bookResponse));

        ResultActions perform = mockMvc.perform(get("http://localhost:8084/v1/library/books")
                .param("pageNumber","0")
                .param("value","10")
        );

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(1000));
    }

    @Test
    void getBooks_NotFound() throws Exception {
        when(bookService.getAllBooks(0,10)).thenReturn(List.of());

        ResultActions perform = mockMvc.perform(get("http://localhost:8084/v1/library/books")
                .param("pageNumber","0")
                .param("value","10")
        );

        perform.andExpect(status().isNotFound());

    }


    @Test
    void getBooksByAuthor_Ok() throws Exception {
        when(bookService.getAllBooksByAuthor(bookResponse.getAuthor(),0,10)).thenReturn(List.of(bookResponse));

        ResultActions perform = mockMvc.perform(get("http://localhost:8084/v1/library/books/author/")
                .param("pageNumber","0")
                .param("value","10")
        );
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author").value("author"));
    }

    @Test
    void getBooksByAuthor_NotFound() throws Exception {
        when(bookService.getAllBooksByAuthor("author",0,10)).thenReturn(List.of());

        ResultActions perform = mockMvc.perform(get("http://localhost:8084/v1/library/books/author/")
                .param("pageNumber","0")
                .param("value","10")
        );

        perform
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookByUniqueCodeAndAuthor_Ok() throws Exception {
        when(bookService.getBookByUniqueCode(uniqueCode)).thenReturn(bookResponse);

        ResultActions perform = mockMvc.perform(get("http://localhost:8084/v1/library/books/Test/" + uniqueCode + "/"));

        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.publisher").value("pub"));
    }

    @Test
    void getBookByUniqueCodeAndAuthor_NotFound() throws Exception {
        when(bookService.getBookByUniqueCode("uniqueCode")).thenThrow(BookNotFoundException.class);

        ResultActions perform = mockMvc.perform(get("http://localhost:8084/v1/library/books/Test/uniqueCode/"));

        perform.andExpect(status().isNotFound());

    }

    @Test
    void deleteBookByUniqueCode_OK() throws Exception{
        String uniqueCode = "werty123";
        doNothing().when(bookService).deleteBookByUniqueCode(uniqueCode);

        ResultActions perform = mockMvc.perform(delete("http://localhost:8084/v1/library/books/Test/" + uniqueCode + "/"));

        perform.andExpect(status().isOk());

    }

    @Test
    void deleteBookByUniqueCode_NotFound() throws Exception{
        String uniqueCode = "werty123";
        doThrow(BookNotFoundException.class).when(bookService).deleteBookByUniqueCode(uniqueCode);

        ResultActions perform = mockMvc.perform(delete("http://localhost:8084/v1/library/books/Test/" + uniqueCode + "/"));

        perform.andExpect(status().isNotFound());
    }
}