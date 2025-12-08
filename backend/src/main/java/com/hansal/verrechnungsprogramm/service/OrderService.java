package com.hansal.verrechnungsprogramm.service;

import com.hansal.verrechnungsprogramm.model.*;
import com.hansal.verrechnungsprogramm.repository.MeatCutRepository;
import com.hansal.verrechnungsprogramm.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final MeatCutRepository meatCutRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public List<Order> searchOrdersByCustomerName(String customerName) {
        return orderRepository.findByCustomerNameContainingIgnoreCase(customerName);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order createOrder(Order order) {
        // Set product/meat cut references and calculate prices for order items
        for (OrderItem item : order.getItems()) {
            if (item.getMeatCut() != null && item.getMeatCut().getId() != null) {
                // Handle meat cut items
                MeatCut meatCut = meatCutRepository.findById(item.getMeatCut().getId())
                        .orElseThrow(() -> new RuntimeException("Meat cut not found with id: " + item.getMeatCut().getId()));

                item.setMeatCut(meatCut);
                item.setUnitPrice(meatCut.getPricePerKg());
            } else if (item.getProduct() != null && item.getProduct().getId() != null) {
                // Handle product items
                Product product = productService.getProductById(item.getProduct().getId());

                item.setProduct(product);
                item.setUnitPrice(product.getPrice());
            }
            item.setOrder(order);
        }

        order.calculateTotal();
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order orderDetails) {
        Order order = getOrderById(id);

        order.setCustomerName(orderDetails.getCustomerName());
        order.setCustomerPhone(orderDetails.getCustomerPhone());
        order.setCustomerAddress(orderDetails.getCustomerAddress());
        order.setStatus(orderDetails.getStatus());

        // Update items
        order.getItems().clear();
        for (OrderItem item : orderDetails.getItems()) {
            // Keep the unitPrice and weight from the request (allow full editing)
            BigDecimal requestedUnitPrice = item.getUnitPrice();
            BigDecimal requestedWeight = item.getWeight();

            if (item.getMeatCut() != null && item.getMeatCut().getId() != null) {
                // Handle meat cut items
                MeatCut meatCut = meatCutRepository.findById(item.getMeatCut().getId())
                        .orElseThrow(() -> new RuntimeException("Meat cut not found with id: " + item.getMeatCut().getId()));

                item.setMeatCut(meatCut);
                // Use requested price if provided, otherwise use default
                if (requestedUnitPrice == null || requestedUnitPrice.compareTo(BigDecimal.ZERO) == 0) {
                    item.setUnitPrice(meatCut.getPricePerKg());
                }
            } else if (item.getProduct() != null && item.getProduct().getId() != null) {
                // Handle product items
                Product product = productService.getProductById(item.getProduct().getId());

                item.setProduct(product);
                // Use requested price if provided, otherwise use default
                if (requestedUnitPrice == null || requestedUnitPrice.compareTo(BigDecimal.ZERO) == 0) {
                    item.setUnitPrice(product.getPrice());
                }
            }
            // Keep the weight from request
            item.setWeight(requestedWeight);
            order.addItem(item);
        }

        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }

    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
