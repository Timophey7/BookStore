package com.ordersservice.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordersservice.models.CartResponse;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CartResponseDeserializer implements Deserializer<CartResponse> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public CartResponse deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, CartResponse.class);
        } catch (Exception e) {
            throw new SerializationException("Ошибка десериализации CartResponse", e);
        }
    }

    @Override
    public void close() {
    }
}

