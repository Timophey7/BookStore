package com.ordersservice.service;

import com.ordersservice.exceptions.OrderNotFoundException;
import com.ordersservice.models.CartResponse;
import com.ordersservice.models.OrderInfo;
import com.ordersservice.models.OrderInfoResponse;
import com.ordersservice.models.PaymentRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface OrderService {

    @Transactional
    void saveOrderInfo(OrderInfo orderInfo);

    OrderInfo createOrderInfo(CartResponse cartResponse);

    List<OrderInfoResponse> getAllUserOrders(String email) throws OrderNotFoundException;

    void paymentProcessing(String numberOfOrder, PaymentRequest paymentRequest) throws Exception;
}
