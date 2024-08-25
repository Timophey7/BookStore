package com.ordersservice.models;

import lombok.Data;

import java.util.Date;

@Data
public class OrderInfoResponse {

    private Long cartId;
    private Long resultSum;
    private Date dateOfOrder;
    private int count;
    private String userEmail;
    private String numberOfOrder;

}
