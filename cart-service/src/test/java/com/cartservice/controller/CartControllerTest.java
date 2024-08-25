package com.cartservice.controller;

import com.cartservice.exceptions.CartServiceException;
import com.cartservice.models.CartResponse;
import com.cartservice.models.ProductResponse;
import com.cartservice.service.JwtService;
import com.cartservice.service.ProductService;
import com.cartservice.service.impl.CartServiceImpl;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.mockito.Mockito.*;

@WebMvcTest(controllers = CartController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private static final Logger log = LoggerFactory.getLogger(CartControllerTest.class);
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private CartController cartController;

    @MockBean
    Bucket bucket;

    @MockBean
    CartServiceImpl cartService;

    @MockBean
    ProductService productService;

    @MockBean
    HttpSession session;

    @MockBean
    JwtService jwtService;

    private final static String UNIQUE_CODE = "werty123";


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
                .build();
    }

    @Test
    void addProductInCart_Success() throws Exception {

        doNothing().when(cartService).addBookInCart(UNIQUE_CODE,session);

        ResultActions perform = mockMvc.perform(post("/v1/library/cart")
                .param("bookUniqueCode", UNIQUE_CODE)
        );

        perform.andExpect(status().isOk());
    }

    @Test
    void addProductInCart_NotFound() throws Exception {
        MockHttpSession mockHttpSession = new MockHttpSession();
        doThrow(new Exception()).when(cartService).addBookInCart(UNIQUE_CODE,mockHttpSession);

        ResultActions perform = mockMvc.perform(post("/v1/library/cart")
                .session(mockHttpSession)
                .param("bookUniqueCode", UNIQUE_CODE)
        );

        perform.andExpect(status().isNotFound());
    }

    @Test
    void getProductFromCart_OK() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setTitle("testTitle");
        when(cartService.getAllBooksFromCart(httpSession)).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/v1/library/cart")
                        .accept(MediaType.APPLICATION_JSON)
                        .session(httpSession)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("testTitle"));

    }

    @Test
    void deleteProductFromCart_OK() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();
        doNothing().when(productService).deleteProductFromCart(httpSession,UNIQUE_CODE);

        ResultActions perform = mockMvc.perform(delete("/v1/library/cart/" + UNIQUE_CODE)
                .session(httpSession)
        );

        perform.andExpect(status().isOk());
        verify(productService).deleteProductFromCart(httpSession,UNIQUE_CODE);


    }

    @Test
    void createOrder_OK() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(true);
        MockHttpSession mockHttpSession = new MockHttpSession();
        String email = "test@example.com";
        CartResponse cartResponse = new CartResponse();
        cartResponse.setUserEmail(email);
        cartResponse.setCartId(1l);
        when(cartService.makeOrderByUserEmail(email)).thenReturn(cartResponse);

        mockHttpSession.setAttribute("email",email);

        mockMvc.perform(post("/v1/library/cart/createOrder")
                        .session(mockHttpSession)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cartId").value(1l));
    }

    @Test
    void createOrder_BadRequest() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(true);
        MockHttpSession mockHttpSession = new MockHttpSession();
        String email = "test@example.com";
        CartResponse cartResponse = new CartResponse();
        cartResponse.setUserEmail(email);
        cartResponse.setCartId(1l);
        when(cartService.makeOrderByUserEmail(email)).thenThrow(CartServiceException.class);

        mockHttpSession.setAttribute("email",email);

        mockMvc.perform(post("/v1/library/cart/createOrder")
                        .session(mockHttpSession)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_ToManyRequests() throws Exception {
        when(bucket.tryConsume(1)).thenReturn(false);

        mockMvc.perform(post("/v1/library/cart/createOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void incrementCountOfProduct_Success() throws Exception {
        String uniqueCode = "product123";
        MockHttpSession session = new MockHttpSession();

        when(bucket.tryConsume(1)).thenReturn(true);

        mockMvc.perform(post("/v1/library/cart/{uniqueCode}/increment", uniqueCode)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void incrementCountOfProduct_tooManyRequests() throws Exception {
        String uniqueCode = "product123";
        MockHttpSession session = new MockHttpSession();

        when(bucket.tryConsume(1)).thenReturn(false);

        mockMvc.perform(post("/v1/library/cart/{uniqueCode}/increment", uniqueCode)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Too Many Requests"));
    }

    @Test
    void incrementCountOfProduct_exception() throws Exception {
        String uniqueCode = "product123";
        MockHttpSession session = new MockHttpSession();

        when(bucket.tryConsume(1)).thenReturn(true);
        doThrow(new Exception()).when(cartService).incrementCountOfProduct(session, uniqueCode);

        mockMvc.perform(post("/v1/library/cart/{uniqueCode}/increment", uniqueCode)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}