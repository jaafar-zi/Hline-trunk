package com.example.demo.modules.order.interfaces;

import com.example.demo.common.base.dto.BaseResponse;
import com.example.demo.modules.order.application.command.OrderCommandService;
import com.example.demo.modules.order.application.dto.filter.OrderFilter;
import com.example.demo.modules.order.application.dto.request.*;
import com.example.demo.modules.order.application.dto.response.OrderResponse;
import com.example.demo.modules.order.application.dto.response.OrderSummaryResponse;
import com.example.demo.modules.order.application.mapper.OrderMapper;
import com.example.demo.modules.order.application.query.OrderQueryService;
import com.example.demo.modules.order.domain.model.Order;
import com.example.demo.modules.order.domain.model.vo.OrderStatus;
import com.example.demo.modules.order.domain.model.vo.PaymentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order REST Controller
 * 
 * Handles all HTTP requests for order operations
 * Implements CQRS pattern with separate command and query services
 * Uses BaseResponse wrapper for consistent API responses
 * 
 * @author Order Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderCommandService commandService;
    private final OrderQueryService queryService;
    private final OrderMapper orderMapper;

    // ====================================
    // Command Operations (Writes)
    // ====================================

    @PostMapping
    @Operation(
        summary = "Create new order",
        description = "Creates a new order with items and customer information"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Order created successfully",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        
        log.info("REST: Creating new order for customer: {}", request.getCustomerId());
        
        Order order = commandService.createOrder(request);
        OrderResponse response = orderMapper.toDto(order);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(response, "Order created successfully"));
    }

    @PutMapping("/{orderId}")
    @Operation(
        summary = "Update order",
        description = "Updates order details (only allowed for non-shipped orders)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or order cannot be updated"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> updateOrder(
            @Parameter(description = "Order ID")
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderRequest request) {
        
        log.info("REST: Updating order: {}", orderId);
        
        Order order = commandService.updateOrder(orderId, request);
        OrderResponse response = orderMapper.toDto(order);
        
        return ResponseEntity.ok(BaseResponse.success(response, "Order updated successfully"));
    }

    @DeleteMapping("/{orderId}")
    @Operation(
        summary = "Delete order",
        description = "Soft deletes an order (can be restored later)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<BaseResponse<Void>> deleteOrder(
            @Parameter(description = "Order ID")
            @PathVariable UUID orderId) {
        
        log.info("REST: Deleting order: {}", orderId);
        
        commandService.delete(orderId);
        
        return ResponseEntity.ok(BaseResponse.success(null, "Order deleted successfully"));
    }

    // ====================================
    // Order Lifecycle Operations
    // ====================================

    @PostMapping("/{orderId}/confirm")
    @Operation(
        summary = "Confirm order",
        description = "Confirms a pending order, making it ready for payment processing"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order confirmed successfully"),
        @ApiResponse(responseCode = "400", description = "Order cannot be confirmed"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> confirmOrder(
            @Parameter(description = "Order ID")
            @PathVariable UUID orderId) {
        
        log.info("REST: Confirming order: {}", orderId);
        
        Order order = commandService.confirmOrder(orderId);
        OrderResponse response = orderMapper.toDto(order);
        
        return ResponseEntity.ok(BaseResponse.success(response, "Order confirmed successfully"));
    }

    @PostMapping("/{orderId}/payment")
    @Operation(
        summary = "Process payment",
        description = "Records payment information for an order"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payment data"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> processPayment(
            @Parameter(description = "Order ID")
            @PathVariable UUID orderId,
            @Valid @RequestBody ProcessPaymentRequest request) {
        
        log.info("REST: Processing payment for order: {}", orderId);
        
        Order order = commandService.processPayment(orderId, request);
        OrderResponse response = orderMapper.toDto(order);
        
        return ResponseEntity.ok(BaseResponse.success(response, "Payment processed successfully"));
    }

    @PostMapping("/{orderId}/ship")
    @Operation(
        summary = "Ship order",
        description = "Marks an order as shipped with tracking information"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order shipped successfully"),
        @ApiResponse(responseCode = "400", description = "Order cannot be shipped or invalid shipping data"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> shipOrder(
            @Parameter(description = "Order ID")
            @PathVariable UUID orderId,
            @Valid @RequestBody ShipOrderRequest request) {
        
        log.info("REST: Shipping order: {}", orderId);
        
        Order order = commandService.shipOrder(orderId, request);
        OrderResponse response = orderMapper.toDto(order);
        
        return ResponseEntity.ok(BaseResponse.success(response, "Order shipped successfully"));
    }

    @PostMapping("/{orderId}/deliver")
    @Operation(
        summary = "Mark as delivered",
        description = "Marks a shipped order as delivered"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order marked as delivered"),
        @ApiResponse(responseCode = "400", description = "Order is not shipped"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> markAsDelivered(
            @Parameter(description = "Order ID")
            @PathVariable UUID orderId) {
        
        log.info("REST: Marking order as delivered: {}", orderId);
        
        Order order = commandService.markAsDelivered(orderId);
        OrderResponse response = orderMapper.toDto(order);
        
        return ResponseEntity.ok(BaseResponse.success(response, "Order marked as delivered"));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(
        summary = "Cancel order",
        description = "Cancels an order that hasn't been shipped yet"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Order cannot be cancelled"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> cancelOrder(
            @Parameter(description = "Order ID")
            @PathVariable UUID orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        
        log.info("REST: Cancelling order: {}", orderId);
        
        Order order = commandService.cancelOrder(orderId, request);
        OrderResponse response = orderMapper.toDto(order);
        
        return ResponseEntity.ok(BaseResponse.success(response, "Order cancelled successfully"));
    }

    @PostMapping("/{orderId}/refund")
    @Operation(
        summary = "Refund order",
        description = "Issues a refund for a paid order"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order refunded successfully"),
        @ApiResponse(responseCode = "400", description = "Order cannot be refunded"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> refundOrder(
            @Parameter(description = "Order ID")
            @PathVariable UUID orderId,
            @Valid @RequestBody RefundOrderRequest request) {
        
        log.info("REST: Refunding order: {}", orderId);
        
        Order order = commandService.refundOrder(orderId, request);
        OrderResponse response = orderMapper.toDto(order);
        
        return ResponseEntity.ok(BaseResponse.success(response, "Order refunded successfully"));
    }

    // ====================================
    // Query Operations (Reads)
    // ====================================

    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get order by ID",
        description = "Returns full order details including items"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> getOrderById(
            @Parameter(description = "Order ID")
            @PathVariable UUID orderId) {
        
        log.info("REST: Getting order by ID: {}", orderId);
        
        OrderResponse order = queryService.getOrderById(orderId);
        
        return ResponseEntity.ok(BaseResponse.success(order));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(
        summary = "Get order by order number",
        description = "Returns order details by order number"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> getOrderByNumber(
            @Parameter(description = "Order number", example = "ORD-20240115-000001")
            @PathVariable String orderNumber) {
        
        log.info("REST: Getting order by number: {}", orderNumber);
        
        OrderResponse order = queryService.getOrderByNumber(orderNumber);
        
        return ResponseEntity.ok(BaseResponse.success(order));
    }

    @GetMapping("/tracking/{trackingNumber}")
    @Operation(
        summary = "Get order by tracking number",
        description = "Returns order details by shipping tracking number"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> getOrderByTrackingNumber(
            @Parameter(description = "Tracking number")
            @PathVariable String trackingNumber) {
        
        log.info("REST: Getting order by tracking number: {}", trackingNumber);
        
        OrderResponse order = queryService.getOrderByTrackingNumber(trackingNumber);
        
        return ResponseEntity.ok(BaseResponse.success(order));
    }

    @GetMapping
    @Operation(
        summary = "Search orders",
        description = "Search and filter orders with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<BaseResponse<Page<OrderSummaryResponse>>> searchOrders(
            @Parameter(description = "Order filter criteria")
            @ModelAttribute OrderFilter filter) {
        
        log.info("REST: Searching orders with filter");
        
        Page<OrderSummaryResponse> orders = queryService.searchOrders(filter);
        
        return ResponseEntity.ok(BaseResponse.success(
                orders,
                String.format("Found %d orders", orders.getTotalElements())
        ));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(
        summary = "Get customer orders",
        description = "Returns all orders for a specific customer"
    )
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<BaseResponse<List<OrderSummaryResponse>>> getOrdersByCustomer(
            @Parameter(description = "Customer ID")
            @PathVariable UUID customerId) {
        
        log.info("REST: Getting orders for customer: {}", customerId);
        
        List<OrderSummaryResponse> orders = queryService.getOrdersByCustomer(customerId);
        
        return ResponseEntity.ok(BaseResponse.success(
                orders,
                String.format("Found %d orders", orders.size())
        ));
    }

    @GetMapping("/customer/{customerId}/status/{status}")
    @Operation(
        summary = "Get customer orders by status",
        description = "Returns customer orders filtered by status"
    )
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<BaseResponse<List<OrderSummaryResponse>>> getOrdersByCustomerAndStatus(
            @Parameter(description = "Customer ID")
            @PathVariable UUID customerId,
            @Parameter(description = "Order status")
            @PathVariable OrderStatus status) {
        
        log.info("REST: Getting orders for customer: {} with status: {}", customerId, status);
        
        List<OrderSummaryResponse> orders = queryService.getOrdersByCustomerAndStatus(customerId, status);
        
        return ResponseEntity.ok(BaseResponse.success(orders));
    }

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get orders by status",
        description = "Returns all orders with a specific status"
    )
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<BaseResponse<List<OrderSummaryResponse>>> getOrdersByStatus(
            @Parameter(description = "Order status")
            @PathVariable OrderStatus status) {
        
        log.info("REST: Getting orders with status: {}", status);
        
        List<OrderSummaryResponse> orders = queryService.getOrdersByStatus(status);
        
        return ResponseEntity.ok(BaseResponse.success(
                orders,
                String.format("Found %d orders with status %s", orders.size(), status)
        ));
    }

    @GetMapping("/payment-status/{paymentStatus}")
    @Operation(
        summary = "Get orders by payment status",
        description = "Returns all orders with a specific payment status"
    )
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<BaseResponse<List<OrderSummaryResponse>>> getOrdersByPaymentStatus(
            @Parameter(description = "Payment status")
            @PathVariable PaymentStatus paymentStatus) {
        
        log.info("REST: Getting orders with payment status: {}", paymentStatus);
        
        List<OrderSummaryResponse> orders = queryService.getOrdersByPaymentStatus(paymentStatus);
        
        return ResponseEntity.ok(BaseResponse.success(orders));
    }

    // ====================================
    // Operational Queries
    // ====================================

    @GetMapping("/pending/older-than")
    @Operation(
        summary = "Get old pending orders",
        description = "Returns pending orders older than specified date"
    )
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<BaseResponse<List<OrderSummaryResponse>>> getPendingOrdersOlderThan(
            @Parameter(description = "Date threshold")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        
        log.info("REST: Getting pending orders older than: {}", date);
        
        List<OrderSummaryResponse> orders = queryService.getPendingOrdersOlderThan(date);
        
        return ResponseEntity.ok(BaseResponse.success(orders));
    }

    @GetMapping("/awaiting-payment")
    @Operation(
        summary = "Get orders awaiting payment",
        description = "Returns confirmed orders that are awaiting payment"
    )
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<BaseResponse<List<OrderSummaryResponse>>> getOrdersAwaitingPayment() {
        
        log.info("REST: Getting orders awaiting payment");
        
        List<OrderSummaryResponse> orders = queryService.getConfirmedOrdersAwaitingPayment();
        
        return ResponseEntity.ok(BaseResponse.success(orders));
    }

    @GetMapping("/awaiting-shipment")
    @Operation(
        summary = "Get orders awaiting shipment",
        description = "Returns paid orders that are awaiting shipment"
    )
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<BaseResponse<List<OrderSummaryResponse>>> getOrdersAwaitingShipment() {
        
        log.info("REST: Getting orders awaiting shipment");
        
        List<OrderSummaryResponse> orders = queryService.getPaidOrdersAwaitingShipment();
        
        return ResponseEntity.ok(BaseResponse.success(orders));
    }

    @GetMapping("/shipped/not-delivered")
    @Operation(
        summary = "Get shipped orders not yet delivered",
        description = "Returns orders that have been shipped but not yet delivered"
    )
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<BaseResponse<List<OrderSummaryResponse>>> getShippedOrdersNotDelivered() {
        
        log.info("REST: Getting shipped orders not yet delivered");
        
        List<OrderSummaryResponse> orders = queryService.getShippedOrdersNotDelivered();
        
        return ResponseEntity.ok(BaseResponse.success(orders));
    }

    // ====================================
    // Statistics & Analytics
    // ====================================

    @GetMapping("/customer/{customerId}/count")
    @Operation(
        summary = "Count customer orders",
        description = "Returns the total number of orders for a customer"
    )
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    public ResponseEntity<BaseResponse<Long>> countOrdersByCustomer(
            @Parameter(description = "Customer ID")
            @PathVariable UUID customerId) {
        
        log.info("REST: Counting orders for customer: {}", customerId);
        
        long count = queryService.countOrdersByCustomer(customerId);
        
        return ResponseEntity.ok(BaseResponse.success(count));
    }

    @GetMapping("/customer/{customerId}/revenue")
    @Operation(
        summary = "Get customer total revenue",
        description = "Returns the total revenue from a customer's orders"
    )
    @ApiResponse(responseCode = "200", description = "Revenue retrieved successfully")
    public ResponseEntity<BaseResponse<Double>> getTotalRevenueForCustomer(
            @Parameter(description = "Customer ID")
            @PathVariable UUID customerId) {
        
        log.info("REST: Getting total revenue for customer: {}", customerId);
        
        Double revenue = queryService.getTotalRevenueForCustomer(customerId);
        
        return ResponseEntity.ok(BaseResponse.success(revenue));
    }

    @GetMapping("/customer/{customerId}/average-value")
    @Operation(
        summary = "Get customer average order value",
        description = "Returns the average order value for a customer"
    )
    @ApiResponse(responseCode = "200", description = "Average retrieved successfully")
    public ResponseEntity<BaseResponse<Double>> getAverageOrderValueForCustomer(
            @Parameter(description = "Customer ID")
            @PathVariable UUID customerId) {
        
        log.info("REST: Getting average order value for customer: {}", customerId);
        
        Double average = queryService.getAverageOrderValueForCustomer(customerId);
        
        return ResponseEntity.ok(BaseResponse.success(average));
    }

    @GetMapping("/revenue/between")
    @Operation(
        summary = "Get revenue for date range",
        description = "Returns total revenue between two dates"
    )
    @ApiResponse(responseCode = "200", description = "Revenue retrieved successfully")
    public ResponseEntity<BaseResponse<Double>> getTotalRevenueBetween(
            @Parameter(description = "Start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("REST: Getting revenue between {} and {}", startDate, endDate);
        
        Double revenue = queryService.getTotalRevenueBetween(startDate, endDate);
        
        return ResponseEntity.ok(BaseResponse.success(
                revenue,
                String.format("Total revenue: %.2f", revenue)
        ));
    }

    @GetMapping("/number/{orderNumber}/exists")
    @Operation(
        summary = "Check if order number exists",
        description = "Checks whether an order number already exists in the system"
    )
    @ApiResponse(responseCode = "200", description = "Check completed successfully")
    public ResponseEntity<BaseResponse<Boolean>> orderNumberExists(
            @Parameter(description = "Order number to check")
            @PathVariable String orderNumber) {
        
        log.info("REST: Checking if order number exists: {}", orderNumber);
        
        boolean exists = queryService.orderNumberExists(orderNumber);
        
        return ResponseEntity.ok(BaseResponse.success(exists));
    }
}