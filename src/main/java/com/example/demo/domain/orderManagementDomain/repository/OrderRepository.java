package com.example.demo.domain.orderManagementDomain.repository;

import com.example.demo.domain.orderManagementDomain.entity.Order;
import com.example.demo.domain.orderManagementDomain.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
}

