package com.cartservice.service.impl;

import com.cartservice.models.BookResponse;
import com.cartservice.models.Cart;
import com.cartservice.models.Product;
import com.cartservice.models.ProductResponse;
import com.cartservice.repository.CartRepository;
import com.cartservice.repository.ProductRepository;
import com.cartservice.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final WebClient.Builder webClientBuilder;
    private final CartRepository cartRepository;
    @Value("${library.host}")
    private String libraryHost;

    @Override
    public BookResponse fetchBookResponseByUniqueCode(String uniqueCode) throws Exception {
        log.info("libraryHost " + libraryHost);
        return webClientBuilder.build()
                .get()
                .uri("http://"+ libraryHost +":8084/v1/library/books/" + uniqueCode)
                .retrieve()
                .bodyToMono(BookResponse.class)
                .block();
    }

    @Override
    @CacheEvict(value = "product",key = "#uniqueCode")
    public void deleteProductFromCart(HttpSession session, String uniqueCode) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        String email = (String) session.getAttribute("email");

        if (cart != null) {
            cart.removeIf(p -> p.getUniqueCode().equals(uniqueCode));
            session.setAttribute("cart",cart);
        } else if (email != null) {
            Cart cartByUserEmail = cartRepository.findCartByUserEmail(email);
            log.info(cartByUserEmail.getProducts().toString());
            cartByUserEmail.getProducts().removeIf(p -> p.getUniqueCode().equals(uniqueCode));
            cartRepository.save(cartByUserEmail);
        }
    }


    @Override
    @Cacheable(value = "product",key = "#uniqueCode")
    public Product getProductByUniqueCode(String uniqueCode) throws Exception {
        Product productByUniqueCode = productRepository.getProductByUniqueCode(uniqueCode)
                .orElse(null);
        if (productByUniqueCode==null){
            BookResponse bookResponse = fetchBookResponseByUniqueCode(uniqueCode);
            Product product = mapToProduct(bookResponse);
            productRepository.save(product);
            return product;
        }
        return productByUniqueCode;
    }

    @Override
    public ProductResponse mapToProductResponse(Product product) {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setTitle(product.getTitle());
        productResponse.setPrice(product.getPrice());
        productResponse.setPhotoUrl(product.getPhotoUrl());
        productResponse.setUniqueCode(product.getUniqueCode());
        return productResponse;
    }


    @Override
    public Product mapToProduct(BookResponse bookResponse){
        Product product = new Product();
        product.setTitle(bookResponse.getTitle());
        product.setPrice(bookResponse.getPrice());
        product.setUniqueCode(bookResponse.getUniqueCode());
        product.setPhotoUrl(bookResponse.getPhotoUrl());
        return product;
    }

    @Override
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

}
