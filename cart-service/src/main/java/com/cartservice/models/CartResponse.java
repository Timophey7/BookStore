package com.cartservice.models;

import lombok.Data;

@Data
public class CartResponse {

    private String userEmail;
    private Long cartId;
    private Long resultSum;

}
