package com.nextech.service;

import com.nextech.entity.*;
import com.nextech.exception.ResourceNotFoundException;
import com.nextech.repository.OrderRepository;
import com.nextech.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Override
    @Transactional
    public Order checkout(User buyer, String shippingAddress) {
        Cart cart = cartService.getOrCreateCart(buyer);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty");
        }

        Order order = new Order();
        order.setBuyer(buyer);
        order.setShippingAddress(shippingAddress);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for: " + product.getName() +
                        ". Available: " + product.getStock());
            }
            OrderItem orderItem = new OrderItem(order, product,
                    cartItem.getQuantity(), product.getPrice());
            order.getItems().add(orderItem);
            total = total.add(orderItem.getSubtotal());

            // Deduct stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        // Clear cart after successful order
        cartService.clearCart(buyer);
        return saved;
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Override
    public List<Order> findByBuyer(User buyer) {
        return orderRepository.findByBuyerOrderByCreatedAtDesc(buyer);
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Order updateStatus(Long id, OrderStatus status) {
        Order order = findById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public long countByBuyer(User buyer) {
        return orderRepository.countByBuyer(buyer);
    }
}
