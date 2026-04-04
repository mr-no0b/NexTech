package com.nextech.service;

import com.nextech.entity.Cart;
import com.nextech.entity.User;

public interface CartService {
    Cart getOrCreateCart(User buyer);
    Cart addToCart(User buyer, Long productId, int quantity);
    Cart updateCartItem(User buyer, Long cartItemId, int quantity);
    Cart removeCartItem(User buyer, Long cartItemId);
    Cart clearCart(User buyer);
    int getCartItemCount(User buyer);
}
