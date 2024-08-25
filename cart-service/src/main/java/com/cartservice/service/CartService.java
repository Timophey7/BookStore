package com.cartservice.service;

import com.cartservice.exceptions.CartServiceException;
import com.cartservice.exceptions.ProductNotFoundException;
import com.cartservice.models.CartResponse;
import com.cartservice.models.ProductResponse;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface CartService {

    void addBookInCart(String uniqueCode, HttpSession session) throws Exception;

    List<ProductResponse> getAllBooksFromCart(HttpSession session);

    CartResponse makeOrderByUserEmail(String email) throws CartServiceException;

    void incrementCountOfProduct(HttpSession session,String uniqueCode) throws Exception;
}
