package com.cartservice.service.impl;

import com.cartservice.exceptions.CartServiceException;
import com.cartservice.models.*;
import com.cartservice.repository.CartRepository;
import com.cartservice.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final KafkaTemplate<String,CartResponse> kafkaTemplate;
    private final ProductServiceImpl productService;
    private final CartRepository cartRepository;


    @Override
    @Cacheable(value = "books")
    public List<ProductResponse> getAllBooksFromCart(HttpSession session) {
        String email = (String) session.getAttribute("email");
        log.info("Email where get all products:" + email);
        return cartExist(email) ?
                getAllFromCartForRegisteredUser(email) :
                getAllFromCartForUnregisteredUser(session);
    }

    private List<ProductResponse> getAllFromCartForRegisteredUser(String email) {
        Cart cartByUserEmail = cartRepository.findCartByUserEmail(email);
        return cartByUserEmail.getProducts().stream()
                .map(productService::mapToProductResponse)
                .toList();
    }

    private List<ProductResponse> getAllFromCartForUnregisteredUser(HttpSession session) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return List.of();
        }
        return cart.stream()
                .map(productService::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @CachePut(value = "books")
    public void addBookInCart(String uniqueCode, HttpSession session) throws Exception {
        String email = (String) session.getAttribute("email");
        log.info("email add book :"+email);
        if (email == null) {
            addProductToSessionCart(uniqueCode, session);
        } else {
            addProductToUserCart(uniqueCode, email);
        }
    }

    private void addProductToSessionCart(String uniqueCode, HttpSession session) throws Exception {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }
        Product product = productService.getProductByUniqueCode(uniqueCode);
        cart.add(product);
        session.setAttribute("cart", cart);
    }

    private void addProductToUserCart(String uniqueCode, String email) throws Exception {
        Cart cartByUserEmail = cartRepository.findCartByUserEmail(email);
        Product product = productService.getProductByUniqueCode(uniqueCode);

        if (cartByUserEmail == null) {
            cartByUserEmail = new Cart();
            cartByUserEmail.setProducts(new ArrayList<>());
            cartByUserEmail.setDateOfCreate(LocalDate.now());
            cartByUserEmail.setUserEmail(email);
        }
        cartByUserEmail.addProduct(product);
        cartRepository.save(cartByUserEmail);
    }

    private boolean cartExist(String email) {
        return cartRepository.existsByUserEmail(email);
    }

    @Override
    public CartResponse makeOrderByUserEmail(String email) throws CartServiceException {
        if (!cartExist(email)){
            throw new CartServiceException("cart not found");
        }
        CartResponse cartResponse = new CartResponse();
        Cart cartByUserEmail = cartRepository.findCartByUserEmail(email);
        long sum = cartByUserEmail
                .getProducts()
                .stream()
                .collect(Collectors.summingInt(product -> product.getPrice() * product.getCount()));
        cartResponse.setCartId(cartByUserEmail.getId());
        cartResponse.setUserEmail(email);
        cartResponse.setResultSum(sum);
        kafkaTemplate.send("cart",cartResponse);
        log.info("kafka send" + cartResponse);
        return cartResponse;

    }

    @Override
    public void incrementCountOfProduct(HttpSession session, String uniqueCode) throws Exception {
        String email = (String) session.getAttribute("email");
        if (!cartExist(email)){
            incrementCountOfProductToSessionCart(session,uniqueCode);
        }else {
            incrementCountOfProductToUserCart(session,uniqueCode);
        }
    }

    private void incrementCountOfProductToUserCart(HttpSession session, String uniqueCode) throws Exception {
        Product productByUniqueCode = productService.getProductByUniqueCode(uniqueCode);
        productByUniqueCode.setCount(productByUniqueCode.getCount() + 1);
        productService.saveProduct(productByUniqueCode);
    }

    private void incrementCountOfProductToSessionCart(HttpSession session, String uniqueCode) {
        session.getAttribute("cart");
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        Product product = cart.stream()
                .filter(c -> c.getUniqueCode().equals(uniqueCode))
                .collect(Collectors.toList()).get(0);
        product.setCount(product.getCount() + 1);
        cart.stream()
                .filter(c -> c.getUniqueCode().equals(uniqueCode))
                .map(c -> cart.remove(c));
        cart.add(product);

    }
}
