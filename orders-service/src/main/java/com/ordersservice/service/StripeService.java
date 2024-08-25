package com.ordersservice.service;

import com.stripe.model.Charge;

public interface StripeService {

    Charge charge(String token, Long amount) throws Exception;

}
