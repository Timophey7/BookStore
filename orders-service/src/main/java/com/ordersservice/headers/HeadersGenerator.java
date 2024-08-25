package com.ordersservice.headers;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;


@Service
public class HeadersGenerator {

    public HttpHeaders getHeaderForSuccessGetMethod(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return headers;
    }

    public HttpHeaders getHeadersForError(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/problem+json; charset=UTF-8");
        return headers;
    }

    public HttpHeaders getHeadersForSuccessPostMethod() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        return httpHeaders;
    }


}
