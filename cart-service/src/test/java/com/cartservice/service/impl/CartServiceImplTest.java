package com.cartservice.service.impl;

import com.cartservice.exceptions.CartServiceException;
import com.cartservice.models.Cart;
import com.cartservice.models.CartResponse;
import com.cartservice.models.Product;
import com.cartservice.models.ProductResponse;
import com.cartservice.repository.CartRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.kafka.common.protocol.types.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @InjectMocks
    CartServiceImpl cartService;

    @Mock
    KafkaTemplate<String, CartResponse> kafkaTemplate;

    @Mock
    ProductServiceImpl productService;

    @Mock
    CartRepository cartRepository;

    @Mock
    HttpSession session;

    List<Product> products;
    Product product;
    Cart cart;

    private static final String email = "test@gmail.com";
    private static final String uniqueCode = "werty123";

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1);
        product.setPrice(1000);
        product.setTitle("title");
        product.setUniqueCode(uniqueCode);
        products = new ArrayList<>();
        products.add(product);
        cart = new Cart();
        cart.setId(1l);
        cart.setProducts(products);
        cart.setUserEmail(email);
    }



    @Test
    void getAllBooksFromUserCart() {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setTitle("title");
        when(session.getAttribute("email")).thenReturn(email);
        when(cartRepository.existsByUserEmail(email)).thenReturn(true);
        when(cartRepository.findCartByUserEmail(email)).thenReturn(cart);
        when(productService.mapToProductResponse(product)).thenReturn(productResponse);

        List<ProductResponse> allBooksFromCart = cartService.getAllBooksFromCart(session);

        verify(cartRepository).existsByUserEmail(email);
        verify(cartRepository).findCartByUserEmail(email);
        assertEquals(product.getTitle(),allBooksFromCart.get(0).getTitle());


    }

    @Test
    void getAllBooksFromSessionCart() {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setTitle("title");
        when(session.getAttribute("email")).thenReturn(email);
        when(cartRepository.existsByUserEmail(email)).thenReturn(false);
        when(session.getAttribute("cart")).thenReturn(products);
        when(productService.mapToProductResponse(product)).thenReturn(productResponse);

        List<ProductResponse> allBooksFromCart = cartService.getAllBooksFromCart(session);

        verify(cartRepository).existsByUserEmail(email);
        verify(session).getAttribute("cart");
        assertEquals(product.getTitle(),allBooksFromCart.get(0).getTitle());


    }

    @Test
    void addBookInSessionCart() throws Exception {
        when(session.getAttribute("email")).thenReturn(null);
        when(session.getAttribute("cart")).thenReturn(products);
        when(productService.getProductByUniqueCode(uniqueCode)).thenReturn(product);

        cartService.addBookInCart(uniqueCode,session);

        verify(session).getAttribute("email");
        verify(session).getAttribute("cart");
        verify(session).setAttribute("cart",products);

    }

    @Test
    void addBookInSUserCart() throws Exception {
        when(session.getAttribute("email")).thenReturn(email);
        when(cartRepository.findCartByUserEmail(email)).thenReturn(cart);
        when(productService.getProductByUniqueCode(uniqueCode)).thenReturn(product);
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.addBookInCart(uniqueCode,session);

        verify(session).getAttribute("email");
        verify(cartRepository).save(cart);

    }

    @Test
    void makeOrderByUserEmail() throws CartServiceException {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setUserEmail(email);
        cartResponse.setCartId(1l);
        cartResponse.setResultSum(1000l);

        when(cartRepository.existsByUserEmail(email)).thenReturn(true);
        when(cartRepository.findCartByUserEmail(email)).thenReturn(cart);

        CartResponse cartResponse1 = cartService.makeOrderByUserEmail(email);

        assertEquals(cartResponse,cartResponse1);
    }

    @Test
    void makeOrderByUserEmailShouldThrowsCartServiceException() throws CartServiceException {
        when(cartRepository.existsByUserEmail(email)).thenReturn(false);

        CartServiceException exception = assertThrows(CartServiceException.class, () -> {
            cartService.makeOrderByUserEmail(email);
        });
        assertEquals("cart not found", exception.getMessage());

        verify(kafkaTemplate, never()).send(anyString(), any());
    }

}