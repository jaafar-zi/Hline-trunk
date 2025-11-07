package com.example.demo.domain.orderManagementDomain.controller;

import com.example.demo.domain.orderManagementDomain.dto.OrderDto;
import com.example.demo.domain.orderManagementDomain.entity.OrderStatus;
import com.example.demo.domain.orderManagementDomain.entity.PlaceOrderRequest;
import com.example.demo.domain.orderManagementDomain.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Page<OrderDto> listOrders(Pageable pageable) {
        return orderService.getOrders(pageable);
    }

    @PostMapping
    public ResponseEntity<OrderDto> placeOrder(
            @Valid @RequestBody PlaceOrderRequest req
    ) {
        OrderDto dto = orderService.placeOrder(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/{id}/cancel")
    public OrderDto cancelOrder(@PathVariable("id") Long id) {
        return orderService.cancelOrder(id);
    }

    @PostMapping("/{id}/process")
    public OrderDto startProcessing(@PathVariable("id") Long id) {
        return orderService.startProcessing(id);
    }

    @PostMapping("/{id}/ship")
    public OrderDto shipOrder(@PathVariable("id") Long id) {
        return orderService.shipOrder(id);
    }

    @PostMapping("/{id}/deliver")
    public OrderDto deliverOrder(@PathVariable("id") Long id) {
        return orderService.deliverOrder(id);
    }

    @PostMapping("/{id}/return")
    public OrderDto returnOrder(@PathVariable("id") Long id) {
        return orderService.returnOrder(id);
    }

    @GetMapping("/status/{status}")
    public List<OrderDto> getByStatus(@PathVariable OrderStatus status) {
        return orderService.getOrdersByStatus(status);
    }

    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable("id") Long id) {
        return orderService.getOrderById(id);
    }
}
