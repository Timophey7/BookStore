package com.cartservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateEvent {

    private Long cartId;
    private int resultSum;
    private String email;

}
