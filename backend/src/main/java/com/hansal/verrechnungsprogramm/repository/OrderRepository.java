package com.hansal.verrechnungsprogramm.repository;

import com.hansal.verrechnungsprogramm.model.Order;
import com.hansal.verrechnungsprogramm.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerNameContainingIgnoreCase(String customerName);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByCustomerPhone(String phone);
}
