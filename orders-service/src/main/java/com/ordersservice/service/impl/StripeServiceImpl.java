package com.ordersservice.service.impl;

import com.ordersservice.service.StripeService;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeServiceImpl implements StripeService {

    public StripeServiceImpl(@Value("${stripe.api.key.secret}") String apiKey) {
        Stripe.apiKey = apiKey;
    }

    public Charge charge(String token, Long amount) throws Exception {
        ChargeCreateParams params = ChargeCreateParams.builder()
                .setAmount(amount)
                .setCurrency("rub")
                .setSource(token)
                .build();

        return Charge.create(params);
    }

}
