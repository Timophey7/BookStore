package com.ordersservice.controllers;

import com.ordersservice.exceptions.OrderNotFoundException;
import com.ordersservice.headers.HeadersGenerator;
import com.ordersservice.models.OrderInfo;
import com.ordersservice.models.OrderInfoResponse;
import com.ordersservice.models.PaymentRequest;
import com.ordersservice.models.PaymentStatus;
import com.ordersservice.repository.OrderInfoRepository;
import com.ordersservice.service.OrderService;
import com.ordersservice.service.StripeService;
import com.stripe.model.Charge;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/v1/library")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final HeadersGenerator headersGenerator;

    private final OrderService orderService;


    @GetMapping("/orders")
    public ResponseEntity<List<OrderInfoResponse>> getAllOrders(HttpSession session){
        String email = (String) session.getAttribute("email");
        log.info("email in controller :" + email);
        try {
            List<OrderInfoResponse> allUserOrders = orderService.getAllUserOrders(email);
            return new ResponseEntity<>(
                    allUserOrders,
                    headersGenerator.getHeaderForSuccessGetMethod(),
                    HttpStatus.OK
            );
        }catch (OrderNotFoundException exception){
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/orders/{numberOfOrder}/pay")
    public ResponseEntity<?> payForOrder(@PathVariable("numberOfOrder") String numberOfOrder,
                                         @RequestBody PaymentRequest paymentRequest)
    {
        try {
            orderService.paymentProcessing(numberOfOrder,paymentRequest);
            return ResponseEntity
                    .ok()
                    .body("Payment successful, order created!");

        }catch (OrderNotFoundException exception){
            return ResponseEntity.notFound().build();
        }catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment failed: " + exception.getMessage());
        }
    }

}
