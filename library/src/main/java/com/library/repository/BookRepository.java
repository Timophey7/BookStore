package com.library.repository;

import com.library.models.Book;
import com.library.models.BookFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    @Query(value = "SELECT * FROM library.books LIMIT :value OFFSET :offset", nativeQuery = true)
    List<Book> getAllBooks(@Param("offset") int offset, @Param("value") int value);

    @Query(value = "SELECT * FROM library.books WHERE author = :author LIMIT :value OFFSET :offset", nativeQuery = true)
    List<Book> getAllBooksByAuthor(@Param("author") String author,@Param("offset") int offset, @Param("value") int value);

    Book findBookByTitleAndBookFormat(String title, BookFormat format);

    Optional<Book> findBookByUniqueCode(String uniqueCode);
}
