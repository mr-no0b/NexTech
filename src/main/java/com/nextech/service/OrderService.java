package com.nextech.service;

import com.nextech.entity.Order;
import com.nextech.entity.OrderStatus;
import com.nextech.entity.User;

import java.util.List;

public interface OrderService {
    Order checkout(User buyer, String shippingAddress);
    Order findById(Long id);
    List<Order> findByBuyer(User buyer);
    List<Order> findAll();
    Order updateStatus(Long id, OrderStatus status);
    long countByBuyer(User buyer);
}
