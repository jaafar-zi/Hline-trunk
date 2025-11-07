package com.example.demo.domain.orderManagementDomain.repository;

import com.example.demo.domain.orderManagementDomain.entity.OrderShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderShipmentRepository extends JpaRepository<OrderShipment, Long> {

    // Custom query to find shipments by order ID
    OrderShipment findByOrderId(Long orderId);

    // Check existence by ID
    boolean existsById(Long id);
}

