package com.ordersservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordersservice.exceptions.OrderNotFoundException;
import com.ordersservice.headers.HeadersGenerator;
import com.ordersservice.models.OrderInfoResponse;
import com.ordersservice.models.PaymentRequest;
import com.ordersservice.service.JwtService;
import com.ordersservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @MockBean
    JwtService jwtService;
    @MockBean
    HeadersGenerator headersGenerator;
    @MockBean
    OrderService orderService;

    @BeforeEach
    void setUp(){
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllOrders_Success() throws Exception {
        OrderInfoResponse orderInfoResponse = new OrderInfoResponse();
        orderInfoResponse.setCartId(1l);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("email","test@gmail.com");
        when(orderService.getAllUserOrders("test@gmail.com")).thenReturn(List.of(orderInfoResponse));

        ResultActions perform = mockMvc.perform(get("/v1/library/orders")
                .session(session)
        );

        perform.andExpect(status().isOk());
    }

    @Test
    void getAllOrders_ThrowsOrderNotFoundException() throws Exception {
        OrderInfoResponse orderInfoResponse = new OrderInfoResponse();
        orderInfoResponse.setCartId(1l);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("email","test@gmail.com");
        when(orderService.getAllUserOrders("test@gmail.com")).thenThrow(new OrderNotFoundException("not found"));

        ResultActions perform = mockMvc.perform(get("/v1/library/orders")
                .session(session)
        );

        perform.andExpect(status().isNotFound());
    }

    @Test
    void payForOrder_Ok() throws Exception {
        String numberOfOrder = "123";
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setNumberOfOrder(numberOfOrder);
        doNothing().when(orderService).paymentProcessing(numberOfOrder,paymentRequest);

        ResultActions perform = mockMvc.perform(post("/v1/library/orders/{numberOfOrder}/pay", numberOfOrder)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest))
        );

        perform.andExpect(status().isOk())
                .andExpect(content().string("Payment successful, order created!"));


    }

    @Test
    void payForOrder_ThrowsOrderNotFoundException() throws Exception {
        String numberOfOrder = "123";
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setNumberOfOrder(numberOfOrder);
        doThrow(new OrderNotFoundException("not found")).when(orderService).paymentProcessing(numberOfOrder,paymentRequest);

        ResultActions perform = mockMvc.perform(post("/v1/library/orders/{numberOfOrder}/pay", numberOfOrder)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest))
        );

        perform.andExpect(status().isNotFound());


    }

    @Test
    void payForOrder_ThrowsException_PaymentFailed() throws Exception {
        String numberOfOrder = "123";
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setNumberOfOrder(numberOfOrder);
        doThrow(new Exception("failed")).when(orderService).paymentProcessing(numberOfOrder,paymentRequest);

        ResultActions perform = mockMvc.perform(post("/v1/library/orders/{numberOfOrder}/pay", numberOfOrder)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest))
        );

        perform.andExpect(status().isInternalServerError())
                .andExpect(content().string("Payment failed: failed"));


    }
}