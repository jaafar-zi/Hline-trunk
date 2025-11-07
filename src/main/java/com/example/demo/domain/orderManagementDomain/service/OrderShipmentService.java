package com.example.demo.domain.orderManagementDomain.service;

import com.example.demo.global.exceptions.CustomBadRequestException;
import com.example.demo.global.exceptions.CustomInternalServerErrorException;
import com.example.demo.global.exceptions.ResourceNotFoundException;
import com.example.demo.domain.orderManagementDomain.dto.OrderShipmentDto;
import com.example.demo.domain.orderManagementDomain.entity.OrderShipment;
import com.example.demo.domain.orderManagementDomain.entity.ShipmentStatus;
import com.example.demo.domain.orderManagementDomain.repository.OrderShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class OrderShipmentService {

    @Autowired
    private OrderShipmentRepository shipmentRepository;


    // Create a new shipment
    public OrderShipmentDto createShipment(OrderShipmentDto shipmentDto) {
        try {
            // Validate and populate missing attributes
            validateAndFillShipmentDetails(shipmentDto);

            // Convert DTO to Entity
            OrderShipment shipment = toEntity(shipmentDto);

            // Save Entity
            shipment = shipmentRepository.save(shipment);

            // Convert Entity back to DTO
            return toDto(shipment);
        } catch (DataIntegrityViolationException ex) {
            throw new CustomBadRequestException("Invalid data: " + ex.getMessage());
        } catch (Exception ex) {
            throw new CustomInternalServerErrorException("An unexpected error occurred.");
        }
    }

    // Update an existing shipment
    public OrderShipmentDto updateShipment(Long shipmentId, OrderShipmentDto shipmentDto) {
        try {
            // Retrieve the existing shipment
            OrderShipment existingShipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with ID: " + shipmentId));

            // Validate and populate missing attributes
            validateAndFillShipmentDetails(shipmentDto);

            // Update the entity from DTO
            updateEntityFromDto(shipmentDto, existingShipment);

            // Save the updated shipment
            OrderShipment updatedShipment = shipmentRepository.save(existingShipment);

            // Return the updated DTO
            return toDto(updatedShipment);
        } catch (Exception ex) {
            throw new CustomInternalServerErrorException("An error occurred while updating the shipment: " + ex.getMessage());
        }
    }

    // Retrieve a shipment by ID
    public OrderShipmentDto getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with ID: " + id));
    }

    // Retrieve shipments by order ID
    public OrderShipmentDto getShipmentsByOrderId(Long orderId) {
        return this.toDto(shipmentRepository.findByOrderId(orderId));
    }

    // Delete a shipment by ID
    public void deleteShipment(Long id) {
        if (!shipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipment not found with ID: " + id);
        }
        shipmentRepository.deleteById(id);
    }

    // Check if a shipment exists by ID
    public boolean existsById(Long id) {
        return shipmentRepository.existsById(id);
    }

    // Private helper to validate and fill missing attributes in the DTO
    private void validateAndFillShipmentDetails(OrderShipmentDto shipmentDto) {
        if (shipmentDto.getShipmentStatus() == null) {
            shipmentDto.setShipmentStatus("Pending"); // Default status
        }

        if (shipmentDto.getEstimatedDeliveryDate() == null) {
            shipmentDto.setEstimatedDeliveryDate(LocalDate.now().plusDays(7)); // Default 7 days from now
        }

        if (shipmentDto.getDeliveryDuration() == null) {
            shipmentDto.setDeliveryDuration(7); // Default delivery duration
        }

        if (shipmentDto.getCourierName() == null || shipmentDto.getTrackingNumber() == null) {
            throw new CustomBadRequestException("Courier name and tracking number are required.");
        }
    }

    public OrderShipmentDto toDto(OrderShipment orderShipment) {
        if (orderShipment == null) {
            return null;
        }

        OrderShipmentDto dto = new OrderShipmentDto();
        dto.setShipmentId(orderShipment.getShipmentId());
        dto.setOrderId(orderShipment.getOrder().getId());
        dto.setCourierName(orderShipment.getCourierName());
        dto.setTrackingNumber(orderShipment.getTrackingNumber());
        dto.setOrigin(orderShipment.getOrigin());
        dto.setDestination(orderShipment.getDestination());
        dto.setShipmentStatus(orderShipment.getShipmentStatus().name());
        dto.setEstimatedDeliveryDate(orderShipment.getEstimatedDeliveryDate());
        dto.setDeliveryDuration(orderShipment.getDeliveryDuration());
        dto.setShipmentCost(orderShipment.getOrder().getShippingCost());

        return dto;
    }

    public OrderShipment toEntity(OrderShipmentDto orderShipmentDto) {
        if (orderShipmentDto == null) {
            return null;
        }

        OrderShipment entity = new OrderShipment();
        entity.setShipmentId(orderShipmentDto.getShipmentId());
        // Assuming Order is fetched separately and set to the entity
        entity.setCourierName(orderShipmentDto.getCourierName());
        entity.setTrackingNumber(orderShipmentDto.getTrackingNumber());
        entity.setOrigin(orderShipmentDto.getOrigin());
        entity.setDestination(orderShipmentDto.getDestination());
        entity.setShipmentStatus(ShipmentStatus.valueOf(orderShipmentDto.getShipmentStatus()));
        entity.setEstimatedDeliveryDate(orderShipmentDto.getEstimatedDeliveryDate());
        entity.setDeliveryDuration(orderShipmentDto.getDeliveryDuration());

        return entity;
    }


    void updateEntityFromDto(OrderShipmentDto dto, OrderShipment entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getCourierName() != null) {
            entity.setCourierName(dto.getCourierName());
        }
        if (dto.getTrackingNumber() != null) {
            entity.setTrackingNumber(dto.getTrackingNumber());
        }
        if (dto.getOrigin() != null) {
            entity.setOrigin(dto.getOrigin());
        }
        if (dto.getDestination() != null) {
            entity.setDestination(dto.getDestination());
        }
        if (dto.getShipmentStatus() != null) {
            entity.setShipmentStatus(ShipmentStatus.valueOf(dto.getShipmentStatus()));
        }
        if (dto.getEstimatedDeliveryDate() != null) {
            entity.setEstimatedDeliveryDate(dto.getEstimatedDeliveryDate());
        }
        if (dto.getDeliveryDuration() != null) {
            entity.setDeliveryDuration(dto.getDeliveryDuration());
        }
    }

}


