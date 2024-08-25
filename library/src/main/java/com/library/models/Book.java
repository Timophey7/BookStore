package com.library.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "books", indexes = {
        @Index(columnList = "author"),
        @Index(columnList = "book_type")
})
public class Book implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotEmpty
    @Column(unique = true)
    private String title;
    @NotEmpty
    @Column(name = "author")
    private String author;
    @NotEmpty
    private String isbn;
    private String publisher;
    private LocalDate publishedDate;
    @Column(name = "unique_code",unique = true)
    private String uniqueCode;
    @NotEmpty
    private String description;
    @NotEmpty
    private String photoUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "book_type")
    private BookType bookType;
    @Enumerated(EnumType.STRING)
    private BookFormat bookFormat;
    private int price;
}
