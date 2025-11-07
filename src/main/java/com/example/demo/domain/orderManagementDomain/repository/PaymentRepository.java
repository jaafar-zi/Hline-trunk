package com.example.demo.domain.orderManagementDomain.repository;

import com.example.demo.domain.orderManagementDomain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
