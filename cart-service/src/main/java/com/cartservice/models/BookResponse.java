package com.cartservice.models;

import lombok.Data;

@Data
public class BookResponse {
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
