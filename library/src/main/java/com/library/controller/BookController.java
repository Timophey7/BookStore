package com.library.controller;

import com.library.exceptions.BookNotFoundException;
import com.library.http.headers.HeadersGenerator;
import com.library.models.Book;
import com.library.models.BookDTO;
import com.library.models.BookResponse;
import com.library.service.impl.BookServiceImpl;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/v1/library")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final Bucket bucket;

    private final BookServiceImpl bookService;

    private final HeadersGenerator headersGenerator;

//    @PostMapping(value = "/books/addBook")
//    public ResponseEntity<String> addBook(
//            @RequestBody BookDTO book
//    ) {
//            bookService.saveBook(book);
//            return new ResponseEntity<String>(
//                    "successfully saved",
//                    headersGenerator.getHeadersForSuccessPostMethod(),
//                    HttpStatus.CREATED
//            );
//    }

    @PostMapping(value = "/books/addBook")
    public ResponseEntity<String> addBook(
            @RequestBody BookDTO book
    ) {
        if (bucket.tryConsume(1)) {
            if (!bookService.bookExists(book.getTitle(), book.getBookFormat())) {
                bookService.saveBook(book);
                return new ResponseEntity<String>(
                        "successfully saved",
                        headersGenerator.getHeadersForSuccessPostMethod(),
                        HttpStatus.CREATED
                );
            }
            return new ResponseEntity<String>(
                    headersGenerator.getHeadersForError(),
                    HttpStatus.BAD_REQUEST
            );
        }else {
            return new ResponseEntity<>("Too Many Requests", HttpStatus.TOO_MANY_REQUESTS);
        }

    }

    @GetMapping(value = "/books/{uniqueCode}")
    public ResponseEntity<BookResponse> getBookByUniqueCode(@PathVariable("uniqueCode")String uniqueCode){
        try {
            BookResponse bookResponse = bookService.getBookByUniqueCode(uniqueCode);
            return new ResponseEntity<BookResponse>(
                    bookResponse,
                    headersGenerator.getHeaderForSuccessGetMethod(),
                    HttpStatus.OK
            );
        }catch (BookNotFoundException ex) {
            log.error(ex.getMessage());
            return new ResponseEntity<>(
                    headersGenerator.getHeadersForError(),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @GetMapping(value = "/books")
    public ResponseEntity<List<BookResponse>> getBooks(@RequestParam("pageNumber") int pageNum,@RequestParam("value") int value){
        List<BookResponse> allBooks = bookService.getAllBooks(pageNum,value);
        if (!allBooks.isEmpty()){
            return new ResponseEntity<List<BookResponse>>(
                    allBooks,
                    headersGenerator.getHeaderForSuccessGetMethod(),
                    HttpStatus.OK
            );
        }
        return new ResponseEntity<List<BookResponse>>(
                headersGenerator.getHeadersForError(),
                HttpStatus.NOT_FOUND
        );
    }

    @GetMapping(value = "/books/{author}/")
    public ResponseEntity<List<BookResponse>> getBooksByAuthor(
            @PathVariable("author")String author,
            @RequestParam("pageNumber") int pageNum,
            @RequestParam("value") int value
    ){
        List<BookResponse> allBooks = bookService.getAllBooksByAuthor(author,pageNum,value);
        if (!allBooks.isEmpty()){
            log.info("Вызов метода");
            return new ResponseEntity<List<BookResponse>>(
                    allBooks,
                    headersGenerator.getHeaderForSuccessGetMethod(),
                    HttpStatus.OK
            );
        }
        return new ResponseEntity<List<BookResponse>>(
                headersGenerator.getHeadersForError(),
                HttpStatus.NOT_FOUND
        );
    }

    @GetMapping("/books/{author}/{uniqueCode}/")
    public ResponseEntity<BookResponse> getBookByUniqueCodeAndAuthor(@PathVariable("uniqueCode") String uniqueCode){
        try {
            BookResponse bookResponse = bookService.getBookByUniqueCode(uniqueCode);
            return new ResponseEntity<BookResponse>(
                    bookResponse,
                    headersGenerator.getHeaderForSuccessGetMethod(),
                    HttpStatus.OK
            );
        }catch (BookNotFoundException ex){
            log.error(ex.getMessage());
            return new ResponseEntity<>(
                    headersGenerator.getHeadersForError(),
                    HttpStatus.NOT_FOUND
            );
        }

    }

    @DeleteMapping("/books/{author}/{uniqueCode}/")
    public ResponseEntity<Book> deleteBookByUniqueCode(@PathVariable("uniqueCode") String uniqueCode){
        try {
            bookService.deleteBookByUniqueCode(uniqueCode);
            return new ResponseEntity<Book>(
                    headersGenerator.getHeaderForSuccessGetMethod(),
                    HttpStatus.OK
            );
        }catch (BookNotFoundException exception){
            return ResponseEntity.notFound().build();
        }

    }




}
