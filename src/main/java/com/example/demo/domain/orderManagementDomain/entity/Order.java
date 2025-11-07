package com.example.demo.domain.orderManagementDomain.entity;

import com.example.demo.domain.userManagementDomain.address.entity.Address;
import com.example.demo.domain.userManagementDomain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    private LocalDateTime orderDate;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // NEW, PROCESSING, SHIPPED, DELIVERED, CANCELLED, RETURNED

    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // CARD, PAYPAL, BANK_TRANSFER, CASH_ON_DELIVERY


    @ManyToOne(optional = false)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalAmount;

    private BigDecimal shippingCost;

    //we should include the subtotal,tax and total in the order for the pricing

    private String customerNote;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}


