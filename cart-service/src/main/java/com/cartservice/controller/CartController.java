package com.cartservice.controller;

import com.cartservice.exceptions.CartServiceException;
import com.cartservice.models.CartResponse;
import com.cartservice.models.ProductResponse;
import com.cartservice.service.ProductService;
import com.cartservice.service.impl.CartServiceImpl;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/library")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final Bucket bucketIncrement;
    private final Bucket bucketCreateOrder;
    private final CartServiceImpl cartService;
    private final ProductService productService;

    @PostMapping("/cart")
    public ResponseEntity<String> addProductInCart(@RequestParam("bookUniqueCode") String bookUniqueCode, HttpSession session){
        try {
            cartService.addBookInCart(bookUniqueCode, session);
            return ResponseEntity.ok("success");
        }catch (Exception exception){
            return new ResponseEntity<>(
                    "not found",
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @GetMapping("/cart")
    public ResponseEntity<List<ProductResponse>> getProductFromCart(HttpSession session){
        List<ProductResponse> allBooksFromCart = cartService.getAllBooksFromCart(session);
        return new ResponseEntity<>(
                allBooksFromCart,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/cart/{uniqueCode}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable("uniqueCode")String uniqueCode,HttpSession session){
        productService.deleteProductFromCart(session,uniqueCode);
        return new ResponseEntity<>(
                "success",
                HttpStatus.OK
        );
    }

    @PostMapping("/cart/{uniqueCode}/increment")
    public ResponseEntity incrementCountOfProduct(@PathVariable("uniqueCode")String uniqueCode,HttpSession session) {
        if (bucketIncrement.tryConsume(1)) {
            try {
                cartService.incrementCountOfProduct(session, uniqueCode);
                return ResponseEntity.ok().build();
            } catch (Exception exception) {
                return ResponseEntity.badRequest().build();
            }
        }else {
            return new ResponseEntity<>("Too Many Requests", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @PostMapping("/cart/createOrder")
    public ResponseEntity<?> createOrder(HttpSession session){
        if (bucketCreateOrder.tryConsume(1)) {
            String email = (String) session.getAttribute("email");
            try {
                CartResponse cartResponse = cartService.makeOrderByUserEmail(email);
                return new ResponseEntity<>(
                        cartResponse,
                        HttpStatus.OK
                );
            } catch (CartServiceException e) {
                return new ResponseEntity<>(
                        e.getMessage(),
                        HttpStatus.BAD_REQUEST
                );
            }
        }else {
            return new ResponseEntity<>("Too Many Requests", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

}
