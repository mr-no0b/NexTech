package com.nextech.service;

import com.nextech.entity.*;
import com.nextech.exception.ResourceNotFoundException;
import com.nextech.repository.OrderRepository;
import com.nextech.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock CartService cartService;

    @InjectMocks OrderServiceImpl orderService;

    private User buyer;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        buyer = new User();
        buyer.setUsername("buyer1");

        product = new Product();
        product.setName("Mouse");
        product.setPrice(new BigDecimal("49.99"));
        product.setStock(5);

        CartItem item = new CartItem(null, product, 2);

        cart = new Cart(buyer);
        cart.getItems().add(item);
    }

    @Test
    void checkout_success_createsOrder() {
        given(cartService.getOrCreateCart(buyer)).willReturn(cart);
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));
        given(cartService.clearCart(buyer)).willReturn(cart);

        Order result = orderService.checkout(buyer, "123 Main St");

        assertThat(result.getBuyer()).isEqualTo(buyer);
        assertThat(result.getShippingAddress()).isEqualTo("123 Main St");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("99.98"));
        then(cartService).should().clearCart(buyer);
    }

    @Test
    void checkout_emptyCart_throwsException() {
        Cart emptyCart = new Cart(buyer);
        given(cartService.getOrCreateCart(buyer)).willReturn(emptyCart);

        assertThatThrownBy(() -> orderService.checkout(buyer, "123 Main St"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void findById_returnsOrder() {
        Order order = new Order();
        order.setBuyer(buyer);
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        Order result = orderService.findById(1L);

        assertThat(result.getBuyer()).isEqualTo(buyer);
    }

    @Test
    void findByBuyer_returnsBuyerOrders() {
        Order order = new Order();
        given(orderRepository.findByBuyerOrderByCreatedAtDesc(buyer)).willReturn(List.of(order));

        List<Order> results = orderService.findByBuyer(buyer);

        assertThat(results).hasSize(1);
    }

    @Test
    void updateStatus_changesOrderStatus() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(orderRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }
}
