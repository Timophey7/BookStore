package com.cartservice.service;


import com.cartservice.models.BookResponse;
import com.cartservice.models.Product;
import com.cartservice.models.ProductResponse;
import jakarta.servlet.http.HttpSession;

public interface ProductService {
    BookResponse fetchBookResponseByUniqueCode(String uniqueCode) throws Exception;

    Product getProductByUniqueCode(String uniqueCode) throws Exception;

    ProductResponse mapToProductResponse(Product product);

    Product mapToProduct(BookResponse productResponse);

    void saveProduct(Product product);

    void deleteProductFromCart(HttpSession session, String uniqueCode);
}
