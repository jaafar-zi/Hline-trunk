package com.example.demo.domain.orderManagementDomain.service;

import com.example.demo.common.base.service.AbstractBaseService;
import com.example.demo.global.exceptions.CustomBadRequestException;
import com.example.demo.global.exceptions.ResourceNotFoundException;
import com.example.demo.domain.orderManagementDomain.dto.OrderShipmentDto;
import com.example.demo.domain.orderManagementDomain.entity.OrderShipment;
import com.example.demo.domain.orderManagementDomain.entity.ShipmentStatus;
import com.example.demo.domain.orderManagementDomain.repository.OrderShipmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
public class OrderShipmentService extends AbstractBaseService<OrderShipment, Long> {

    private final OrderShipmentRepository shipmentRepository;

    public OrderShipmentService(OrderShipmentRepository shipmentRepository) {
        super(shipmentRepository);
        this.shipmentRepository = shipmentRepository;
    }

    // ───────────────────────────────────────────────
    //  BASE CONFIGURATION
    // ───────────────────────────────────────────────

    @Override
    protected String getCacheName() {
        return "shipments";
    }

    @Override
    protected Class<?> getEntityClass() {
        return OrderShipment.class;
    }

    // ───────────────────────────────────────────────
    //  READ METHODS
    // ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "shipments", key = "'id:' + #shipmentId")
    public OrderShipmentDto getShipmentById(Long shipmentId) {
        return findById(shipmentId)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with ID: " + shipmentId));
    }

//    @Transactional(readOnly = true)
//    @Cacheable(cacheNames = "shipments", key = "'order:' + #orderId")
//    public OrderShipmentDto getShipmentByOrderId(Long orderId) {
//        return shipmentRepository.findByOrderId(orderId).getOrder()
//                .map(this::mapToDto)
//                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for order ID: " + orderId));
//    }

    // ───────────────────────────────────────────────
    //  BUSINESS OPERATIONS
    // ───────────────────────────────────────────────

    @Transactional
    public OrderShipmentDto createShipment(OrderShipmentDto dto) {
        try {
            validateAndFillShipmentDetails(dto);
            OrderShipment shipment = mapToEntity(dto);
            shipment = save(shipment);
            log.info("Shipment created for order {}", dto.getOrderId());
            return mapToDto(shipment);
        } catch (DataIntegrityViolationException e) {
            throw new CustomBadRequestException("Invalid shipment data: " + e.getMessage());
        }
    }

    @Transactional
    public OrderShipmentDto updateShipment(Long shipmentId, OrderShipmentDto dto) {
        OrderShipment existing = findByIdOrThrow(shipmentId);
        validateAndFillShipmentDetails(dto);
        updateEntityFromDto(dto, existing);
        OrderShipment updated = save(existing);
        log.info("Shipment {} updated successfully", shipmentId);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteShipment(Long shipmentId) {
        if (!shipmentRepository.existsById(shipmentId)) {
            throw new ResourceNotFoundException("Shipment not found with ID: " + shipmentId);
        }
        repository.deleteById(shipmentId);
        log.info("Shipment {} deleted", shipmentId);
    }

    // ───────────────────────────────────────────────
    //  HELPER METHODS
    // ───────────────────────────────────────────────

    private void validateAndFillShipmentDetails(OrderShipmentDto dto) {
        if (dto.getShipmentStatus() == null) {
            dto.setShipmentStatus(ShipmentStatus.PENDING.name());
        }
        if (dto.getEstimatedDeliveryDate() == null) {
            dto.setEstimatedDeliveryDate(LocalDate.now().plusDays(7));
        }
        if (dto.getDeliveryDuration() == null) {
            dto.setDeliveryDuration(7);
        }
        if (dto.getCourierName() == null || dto.getTrackingNumber() == null) {
            throw new CustomBadRequestException("Courier name and tracking number are required.");
        }
    }

    private OrderShipmentDto mapToDto(OrderShipment entity) {
        if (entity == null) return null;

        OrderShipmentDto dto = new OrderShipmentDto();
        dto.setShipmentId(entity.getShipmentId());
        dto.setOrderId(entity.getOrder().getId());
        dto.setCourierName(entity.getCourierName());
        dto.setTrackingNumber(entity.getTrackingNumber());
        dto.setOrigin(entity.getOrigin());
        dto.setDestination(entity.getDestination());
        dto.setShipmentStatus(entity.getShipmentStatus().name());
        dto.setEstimatedDeliveryDate(entity.getEstimatedDeliveryDate());
        dto.setDeliveryDuration(entity.getDeliveryDuration());
        dto.setShipmentCost(entity.getOrder().getShippingCost());
        return dto;
    }

    private OrderShipment mapToEntity(OrderShipmentDto dto) {
        if (dto == null) return null;

        OrderShipment entity = new OrderShipment();
        entity.setShipmentId(dto.getShipmentId());
        entity.setCourierName(dto.getCourierName());
        entity.setTrackingNumber(dto.getTrackingNumber());
        entity.setOrigin(dto.getOrigin());
        entity.setDestination(dto.getDestination());
        entity.setShipmentStatus(ShipmentStatus.valueOf(dto.getShipmentStatus()));
        entity.setEstimatedDeliveryDate(dto.getEstimatedDeliveryDate());
        entity.setDeliveryDuration(dto.getDeliveryDuration());
        return entity;
    }

    private void updateEntityFromDto(OrderShipmentDto dto, OrderShipment entity) {
        if (dto == null || entity == null) return;

        if (dto.getCourierName() != null) entity.setCourierName(dto.getCourierName());
        if (dto.getTrackingNumber() != null) entity.setTrackingNumber(dto.getTrackingNumber());
        if (dto.getOrigin() != null) entity.setOrigin(dto.getOrigin());
        if (dto.getDestination() != null) entity.setDestination(dto.getDestination());
        if (dto.getShipmentStatus() != null)
            entity.setShipmentStatus(ShipmentStatus.valueOf(dto.getShipmentStatus()));
        if (dto.getEstimatedDeliveryDate() != null)
            entity.setEstimatedDeliveryDate(dto.getEstimatedDeliveryDate());
        if (dto.getDeliveryDuration() != null)
            entity.setDeliveryDuration(dto.getDeliveryDuration());
    }

    // ───────────────────────────────────────────────
    //  LIFECYCLE HOOKS
    // ───────────────────────────────────────────────

    @Override
    protected void onAfterSave(OrderShipment entity) {
        log.debug("Shipment {} saved with status {}", entity.getShipmentId(), entity.getShipmentStatus());
    }

    @Override
    protected void onAfterDelete(Long id) {
        log.debug("Shipment {} deleted from repository", id);
    }
}


