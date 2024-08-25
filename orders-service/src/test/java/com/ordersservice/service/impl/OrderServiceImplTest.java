package com.ordersservice.service.impl;

import com.ordersservice.exceptions.OrderNotFoundException;
import com.ordersservice.models.CartResponse;
import com.ordersservice.models.OrderInfo;
import com.ordersservice.models.OrderInfoResponse;
import com.ordersservice.models.PaymentRequest;
import com.ordersservice.repository.OrderInfoRepository;
import com.ordersservice.service.StripeService;
import com.stripe.model.Charge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    OrderServiceImpl orderService;

    @Mock
    OrderInfoRepository orderInfoRepository;

    @Mock
    StripeService stripeService;

    OrderInfo orderInfo;

    @BeforeEach
    void setUp() {
        orderInfo = new OrderInfo();
        orderInfo.setOrderId(1);
        orderInfo.setCartId(1l);
        orderInfo.setUserEmail("test@gmail.com");
        orderInfo.setNumberOfOrder("1234qwer");
        orderInfo.setResultSum(1000l);
    }

    @Test
    void saveOrderInfo() {
        when(orderInfoRepository.save(orderInfo)).thenReturn(orderInfo);

        orderService.saveOrderInfo(orderInfo);

        verify(orderInfoRepository,times(1)).save(orderInfo);
    }

    @Test
    void createOrderInfo() {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartId(1l);
        cartResponse.setResultSum(1000l);
        cartResponse.setUserEmail("test@gmail.com");

        OrderInfo orderServiceOrderInfo = orderService.createOrderInfo(cartResponse);

        assertEquals(cartResponse.getCartId(),orderServiceOrderInfo.getCartId());
    }

    @Test
    void getAllUserOrdersShouldReturnListOfOrderInfoResponse() throws OrderNotFoundException {
        String email = "test@gmail.com";
        when(orderInfoRepository.findAllOrderInfoByUserEmail(email)).thenReturn(Optional.of(List.of(orderInfo)));

        List<OrderInfoResponse> allUserOrders = orderService.getAllUserOrders(email);

        assertEquals(allUserOrders.get(0).getUserEmail(), email);
    }

    @Test
    void getAllUserOrdersShouldThrowOrderNotFoundException() throws OrderNotFoundException {
        String email = "test@gmail.com";

        OrderNotFoundException orderNotFoundException = assertThrows(OrderNotFoundException.class, () -> {
            orderService.getAllUserOrders(email);
        });

        assertEquals(orderNotFoundException.getMessage(),"orders not found");
    }

    @Test
    void paymentProcessing() throws Exception {
        String numberOfOrder = orderInfo.getNumberOfOrder();
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setUserEmail("test@gmail.com");
        paymentRequest.setNumberOfOrder(numberOfOrder);
        paymentRequest.setToken("token1213");
        paymentRequest.setResultSum(1000l);
        Charge charge = new Charge();
        charge.setOrder("1");

        when(orderInfoRepository.findOrderInfoByNumberOfOrder(numberOfOrder)).thenReturn(Optional.of(orderInfo));
        when(stripeService.charge(paymentRequest.getToken(),paymentRequest.getResultSum())).thenReturn(charge);
        when(orderInfoRepository.save(orderInfo)).thenReturn(orderInfo);

        orderService.paymentProcessing(numberOfOrder,paymentRequest);

        verify(orderInfoRepository,times(1)).findOrderInfoByNumberOfOrder(numberOfOrder);
        verify(stripeService,times(1)).charge(paymentRequest.getToken(),paymentRequest.getResultSum());
        verify(orderInfoRepository,times(1)).save(orderInfo);








    }
}