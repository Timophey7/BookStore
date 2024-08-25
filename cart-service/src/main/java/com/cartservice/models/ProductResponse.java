package com.cartservice.models;

import lombok.Data;

@Data
public class ProductResponse {
    private String title;
    private int price;
    private String uniqueCode;
    private String photoUrl;
}
