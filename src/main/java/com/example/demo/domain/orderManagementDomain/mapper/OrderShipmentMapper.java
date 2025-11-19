package com.example.demo.domain.orderManagementDomain.mapper;

import com.example.demo.common.mapper.AbstractBaseMapper;
import com.example.demo.domain.orderManagementDomain.dto.OrderShipmentDto;
import com.example.demo.domain.orderManagementDomain.entity.OrderShipment;
import com.example.demo.domain.orderManagementDomain.entity.ShipmentStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderShipmentMapper extends AbstractBaseMapper<OrderShipment, OrderShipmentDto> {

    @Override
    public OrderShipmentDto toDto(OrderShipment entity) {
        if (entity == null) return null;

        OrderShipmentDto dto = new OrderShipmentDto();
        dto.setShipmentId(entity.getShipmentId());
        dto.setOrderId(entity.getOrder() != null ? entity.getOrder().getId() : null);
        dto.setCourierName(entity.getCourierName());
        dto.setTrackingNumber(entity.getTrackingNumber());
        dto.setOrigin(entity.getOrigin());
        dto.setDestination(entity.getDestination());
        dto.setShipmentStatus(entity.getShipmentStatus() != null
                ? entity.getShipmentStatus().name()
                : null);
        dto.setEstimatedDeliveryDate(entity.getEstimatedDeliveryDate());
        dto.setDeliveryDuration(entity.getDeliveryDuration());
        dto.setShipmentCost(entity.getOrder() != null ? entity.getOrder().getShippingCost() : null);

        return dto;
    }

    @Override
    public OrderShipment toEntity(OrderShipmentDto dto) {
        if (dto == null) return null;

        OrderShipment entity = new OrderShipment();
        entity.setShipmentId(dto.getShipmentId());
        entity.setCourierName(dto.getCourierName());
        entity.setTrackingNumber(dto.getTrackingNumber());
        entity.setOrigin(dto.getOrigin());
        entity.setDestination(dto.getDestination());
        entity.setShipmentStatus(dto.getShipmentStatus() != null
                ? ShipmentStatus.valueOf(dto.getShipmentStatus())
                : null);
        entity.setEstimatedDeliveryDate(dto.getEstimatedDeliveryDate());
        entity.setDeliveryDuration(dto.getDeliveryDuration());

        return entity;
    }

    @Override
    public void updateEntityFromDto(OrderShipmentDto dto, OrderShipment entity) {
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
}
