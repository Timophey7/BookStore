package com.library.models;

import lombok.Data;

import java.io.Serializable;

@Data
public class BookResponse implements Serializable {
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String publishedDate;
    private String uniqueCode;
    private String description;
    private String photoUrl;
    private int price;
}

