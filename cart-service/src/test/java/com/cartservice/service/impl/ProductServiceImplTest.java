package com.cartservice.service.impl;

import com.cartservice.models.BookResponse;
import com.cartservice.models.Cart;
import com.cartservice.models.Product;
import com.cartservice.models.ProductResponse;
import com.cartservice.repository.CartRepository;
import com.cartservice.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    ProductServiceImpl productService;

    @Mock
    ProductRepository productRepository;

    @Mock
    WebClient.Builder webClientBuilder;

    @Mock
    CartRepository cartRepository;

    @Mock
    HttpSession session;

    Product product;

    private final static String UNIQUE_CODE = "werty123";

    @BeforeEach
    void setUp() {

        product = new Product();
        product.setUniqueCode("werty123");


    }

    @Test
    void fetchBookResponseByUniqueCode() throws Exception {
        BookResponse bookResponse = new BookResponse();
        bookResponse.setUniqueCode(UNIQUE_CODE);

        WebClient mockWebClient = Mockito.mock(WebClient.class);
        WebClient.Builder mockBuilder = Mockito.mock(WebClient.Builder.class);
        WebClient.RequestHeadersUriSpec mockUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec mockResponseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        when(mockWebClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri("http://null:8084/v1/library/books/" + UNIQUE_CODE))
                .thenReturn(mockUriSpec);
        when(mockUriSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(BookResponse.class)).thenReturn(Mono.just(bookResponse));

        when(webClientBuilder.build()).thenReturn(mockWebClient);

        BookResponse fetchBookResponseByUniqueCode = productService.fetchBookResponseByUniqueCode(UNIQUE_CODE);

        assertEquals(bookResponse.getUniqueCode(),fetchBookResponseByUniqueCode.getUniqueCode());

    }

    @Test
    void getProductByUniqueCodeWhereProductNotNull() throws Exception {

        when(productRepository.getProductByUniqueCode(UNIQUE_CODE)).thenReturn(Optional.of(product));

        Product productByUniqueCode = productService.getProductByUniqueCode(UNIQUE_CODE);

        assertEquals(product,productByUniqueCode);
        verify(productRepository).getProductByUniqueCode(UNIQUE_CODE);

    }

    @Test
    void getProductByUniqueCodeWhereProductIsNull() throws Exception {
        BookResponse bookResponse = new BookResponse();
        bookResponse.setUniqueCode(UNIQUE_CODE);

        when(productRepository.getProductByUniqueCode(UNIQUE_CODE)).thenReturn(Optional.empty());
        WebClient mockWebClient = Mockito.mock(WebClient.class);
        WebClient.Builder mockBuilder = Mockito.mock(WebClient.Builder.class);
        WebClient.RequestHeadersUriSpec mockUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec mockResponseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        when(mockWebClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri("http://null:8084/v1/library/books/" + UNIQUE_CODE))
                .thenReturn(mockUriSpec);
        when(mockUriSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(BookResponse.class)).thenReturn(Mono.just(bookResponse));
        when(webClientBuilder.build()).thenReturn(mockWebClient);
        when(productRepository.save(product)).thenReturn(product);

        Product productByUniqueCode = productService.getProductByUniqueCode(UNIQUE_CODE);

        assertEquals(product.getUniqueCode(),productByUniqueCode.getUniqueCode());
        verify(productRepository).save(product);

    }

    @Test
    void mapToProductResponse() {
        product.setTitle("testTitle");
        product.setPrice(1000);
        ProductResponse productResponse = new ProductResponse();
        productResponse.setUniqueCode(UNIQUE_CODE);
        productResponse.setPrice(1000);
        productResponse.setTitle("testTitle");

        productService.mapToProductResponse(product);

        assertEquals(product.getUniqueCode(),productResponse.getUniqueCode());
        assertEquals(product.getPrice(),productResponse.getPrice());
        assertEquals(product.getTitle(),productResponse.getTitle());


    }

    @Test
    void mapToProduct() {
        product.setTitle("testTitle");
        product.setPrice(1000);
        BookResponse bookResponse = new BookResponse();
        bookResponse.setUniqueCode(UNIQUE_CODE);
        bookResponse.setPrice(1000);
        bookResponse.setTitle("testTitle");

        Product mappedToProduct = productService.mapToProduct(bookResponse);

        assertEquals(product.getTitle(),mappedToProduct.getTitle());
        assertEquals(product.getUniqueCode(),mappedToProduct.getUniqueCode());
    }

    @Test
    void deleteProductFromSessionCart(){
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        when(session.getAttribute("cart")).thenReturn(productList);
        when(session.getAttribute("email")).thenReturn(null);

        productService.deleteProductFromCart(session,product.getUniqueCode());

        assertFalse(productList.contains(product));
    }

    @Test
    void deleteProductFromUserCart(){
        String email = "test@gmail.com";
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        Cart cart = Cart
                .builder()
                .id(1l)
                .products(productList)
                .userEmail(email)
                .build();
        when(session.getAttribute("cart")).thenReturn(null);
        when(session.getAttribute("email")).thenReturn(email);
        when(cartRepository.findCartByUserEmail(email)).thenReturn(cart);

        productService.deleteProductFromCart(session, product.getUniqueCode());

        verify(cartRepository, times(1)).findCartByUserEmail(email);
        verify(cartRepository, times(1)).save(cart);
        assertEquals(0, cart.getProducts().size());
    }
}