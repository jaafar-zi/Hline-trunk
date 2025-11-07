package com.example.demo.domain.orderManagementDomain.controller;

import com.example.demo.domain.orderManagementDomain.dto.OrderShipmentDto;
import com.example.demo.domain.orderManagementDomain.service.OrderShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipments")
public class OrderShipmentController {

    private final OrderShipmentService shipmentService;

    public OrderShipmentController(OrderShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<OrderShipmentDto> createShipment(@RequestBody @Valid OrderShipmentDto shipmentDto) {
        OrderShipmentDto createdShipment = shipmentService.createShipment(shipmentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShipment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderShipmentDto> getShipmentById(@PathVariable Long id) {
        OrderShipmentDto shipmentDto = shipmentService.getShipmentById(id);
        return ResponseEntity.ok(shipmentDto);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<OrderShipmentDto> getShipmentsByOrderId(@PathVariable Long orderId) {
        OrderShipmentDto shipment = shipmentService.getShipmentsByOrderId(orderId);
        return ResponseEntity.ok(shipment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderShipmentDto> updateShipment(
            @PathVariable Long id,
            @RequestBody @Valid OrderShipmentDto shipmentDto) {
        shipmentDto.setShipmentId(id); // Ensure the ID is set correctly
        OrderShipmentDto updatedShipment = shipmentService.updateShipment(id, shipmentDto);
        return ResponseEntity.ok(updatedShipment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }
}


