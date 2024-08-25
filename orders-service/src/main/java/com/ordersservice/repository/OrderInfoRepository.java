package com.ordersservice.repository;

import com.ordersservice.models.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderInfoRepository extends JpaRepository<OrderInfo,Integer> {

    Optional<OrderInfo> findOrderInfoByNumberOfOrder(String numberOfOrder);

    Optional<List<OrderInfo>> findAllOrderInfoByUserEmail(String email);

}
