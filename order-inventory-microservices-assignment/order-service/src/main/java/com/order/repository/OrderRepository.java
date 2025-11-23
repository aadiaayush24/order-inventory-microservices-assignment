package com.order.repository;

import com.order.model.Order;
import com.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderId(String orderId);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByCustomerEmail(String customerEmail);
    
    boolean existsByOrderId(String orderId);
}

