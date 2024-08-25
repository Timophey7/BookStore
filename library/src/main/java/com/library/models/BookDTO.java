package com.library.models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookDTO {
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private LocalDate publishedDate;
    private String uniqueCode;
    private String description;
    private String photoUrl;
    private BookType bookType;
    private BookFormat bookFormat;
    private int price;
}
