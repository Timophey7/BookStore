package com.cartservice.repository;


import com.cartservice.models.Cart;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {

    boolean existsByUserEmail(String userEmail);

    Cart findCartByUserEmail(String userEmail);

}
