package com.ordersservice.service.impl;

import com.ordersservice.exceptions.OrderNotFoundException;
import com.ordersservice.models.*;
import com.ordersservice.repository.OrderInfoRepository;
import com.ordersservice.service.OrderService;
import com.ordersservice.service.StripeService;
import com.stripe.model.Charge;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderInfoRepository orderInfoRepository;
    private final StripeService stripeService;

    @Override
    @CachePut(value = "orders",key = "#orderInfo.userEmail")
    public void saveOrderInfo(OrderInfo orderInfo) {
        orderInfoRepository.save(orderInfo);
    }

    @Override
    public OrderInfo createOrderInfo(CartResponse cartResponse) {
        return OrderInfo.builder()
                .cartId(cartResponse.getCartId())
                .resultSum(cartResponse.getResultSum())
                .dateOfOrder(new Date())
                .userEmail(cartResponse.getUserEmail())
                .numberOfOrder(UUID.randomUUID().toString().substring(0,7))
                .build();

    }

    @Override
    @Cacheable(value = "orders",key = "#email")
    public List<OrderInfoResponse> getAllUserOrders(String email) throws OrderNotFoundException {
        log.info("email in get all orders : "+email);
        return orderInfoRepository.findAllOrderInfoByUserEmail(email)
                .orElseThrow(() -> new OrderNotFoundException("orders not found"))
                .stream()
                .map(this::mapToOrderInfoResponse)
                .collect(Collectors.toList());


    }

    private OrderInfoResponse mapToOrderInfoResponse(OrderInfo orderInfo){
        OrderInfoResponse orderInfoResponse = new OrderInfoResponse();
        orderInfoResponse.setCartId(orderInfo.getCartId());
        orderInfoResponse.setNumberOfOrder(orderInfo.getNumberOfOrder());
        orderInfoResponse.setDateOfOrder(orderInfo.getDateOfOrder());
        orderInfoResponse.setResultSum(orderInfo.getResultSum());
        orderInfoResponse.setUserEmail(orderInfo.getUserEmail());
        return orderInfoResponse;
    }

    @Override
    public void paymentProcessing(String numberOfOrder, PaymentRequest paymentRequest) throws Exception {

        OrderInfo orderInfo = orderInfoRepository.findOrderInfoByNumberOfOrder(numberOfOrder)
                .orElseThrow(() -> new OrderNotFoundException("order not found"));

        paymentRequest.setNumberOfOrder(numberOfOrder);
        paymentRequest.setResultSum(orderInfo.getResultSum());
        Charge charge = stripeService.charge(paymentRequest.getToken(), paymentRequest.getResultSum());

        orderInfo.setPaymentStatus(PaymentStatus.PAID);

        orderInfoRepository.save(orderInfo);

    }
}
