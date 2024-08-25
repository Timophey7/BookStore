package com.ordersservice.models;

import lombok.Data;

@Data
public class PaymentRequest {

    private Long resultSum;
    private String userEmail;
    private String numberOfOrder;
    private String token;

}
