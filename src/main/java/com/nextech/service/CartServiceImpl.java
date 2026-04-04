package com.nextech.service;

import com.nextech.entity.*;
import com.nextech.exception.ResourceNotFoundException;
import com.nextech.repository.CartItemRepository;
import com.nextech.repository.CartRepository;
import com.nextech.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Cart getOrCreateCart(User buyer) {
        return cartRepository.findByBuyer(buyer)
                .orElseGet(() -> cartRepository.save(new Cart(buyer)));
    }

    @Override
    @Transactional
    public Cart addToCart(User buyer, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new IllegalArgumentException("Product is not available");
        }
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStock());
        }

        Cart cart = getOrCreateCart(buyer);
        Optional<CartItem> existing = cartItemRepository.findByCartAndProduct(cart, product);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;
            if (product.getStock() < newQty) {
                throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStock());
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem(cart, product, quantity);
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }
        return cartRepository.findByBuyer(buyer).orElseThrow();
    }

    @Override
    @Transactional
    public Cart updateCartItem(User buyer, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (!item.getCart().getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getStock() < quantity) {
                throw new IllegalArgumentException("Insufficient stock. Available: " + item.getProduct().getStock());
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return getOrCreateCart(buyer);
    }

    @Override
    @Transactional
    public Cart removeCartItem(User buyer, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (!item.getCart().getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        cartItemRepository.delete(item);
        return getOrCreateCart(buyer);
    }

    @Override
    @Transactional
    public Cart clearCart(User buyer) {
        Cart cart = getOrCreateCart(buyer);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    @Override
    public int getCartItemCount(User buyer) {
        return cartRepository.findByBuyer(buyer)
                .map(c -> c.getItems().stream().mapToInt(CartItem::getQuantity).sum())
                .orElse(0);
    }
}
