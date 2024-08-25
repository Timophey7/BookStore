package com.ordersservice.config.kafka;


import com.ordersservice.models.CartResponse;
import com.ordersservice.models.OrderInfo;
import com.ordersservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaListenerOrder {


    private final OrderService orderService;

    @KafkaListener(topics = "cart",groupId = "my-carts")
    public void listen(CartResponse message) {
        OrderInfo orderInfo = orderService.createOrderInfo(message);
        orderService.saveOrderInfo(orderInfo);
        log.info("get new order:"+message.toString());
        log.info("send number of order order:"+orderInfo.getNumberOfOrder());
    }

}
