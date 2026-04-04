package com.nextech.controller;

import com.nextech.dto.CheckoutDto;
import com.nextech.entity.User;
import com.nextech.service.CartService;
import com.nextech.service.OrderService;
import com.nextech.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('BUYER')")
@RequiredArgsConstructor
public class BuyerController {

    private final UserService userService;
    private final CartService cartService;
    private final OrderService orderService;

    private User getCurrentUser(UserDetails ud) {
        return userService.findByUsername(ud.getUsername()).orElseThrow();
    }

    /* ─── Dashboard ─────────────────────────────────────────────────────── */

    @GetMapping("/buyer/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        User buyer = getCurrentUser(ud);
        model.addAttribute("buyer", buyer);
        model.addAttribute("totalOrders", orderService.countByBuyer(buyer));
        model.addAttribute("cartCount",   cartService.getCartItemCount(buyer));
        model.addAttribute("recentOrders", orderService.findByBuyer(buyer).stream().limit(5).toList());
        return "buyer/dashboard";
    }

    /* ─── Cart ───────────────────────────────────────────────────────────── */

    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal UserDetails ud, Model model) {
        User buyer = getCurrentUser(ud);
        var cart = cartService.getOrCreateCart(buyer);
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutDto", new CheckoutDto());
        java.math.BigDecimal cartTotal = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("cartTotal", cartTotal);
        return "buyer/cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@AuthenticationPrincipal UserDetails ud,
                            @RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes ra) {
        User buyer = getCurrentUser(ud);
        try {
            cartService.addToCart(buyer, productId, quantity);
            ra.addFlashAttribute("success", "Item added to cart!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/product/" + productId;
    }

    @PostMapping("/cart/update")
    public String updateCart(@AuthenticationPrincipal UserDetails ud,
                             @RequestParam Long cartItemId,
                             @RequestParam int quantity,
                             RedirectAttributes ra) {
        User buyer = getCurrentUser(ud);
        try {
            cartService.updateCartItem(buyer, cartItemId, quantity);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@AuthenticationPrincipal UserDetails ud,
                                 @RequestParam Long cartItemId,
                                 RedirectAttributes ra) {
        User buyer = getCurrentUser(ud);
        cartService.removeCartItem(buyer, cartItemId);
        ra.addFlashAttribute("success", "Item removed from cart.");
        return "redirect:/cart";
    }

    /* ─── Checkout / Orders ─────────────────────────────────────────────── */

    @PostMapping("/cart/checkout")
    public String checkout(@AuthenticationPrincipal UserDetails ud,
                           @Valid @ModelAttribute CheckoutDto dto,
                           BindingResult result,
                           Model model,
                           RedirectAttributes ra) {
        if (result.hasErrors()) {
            User buyer = getCurrentUser(ud);
            var cart = cartService.getOrCreateCart(buyer);
            model.addAttribute("cart", cart);
            java.math.BigDecimal cartTotal = cart.getItems().stream()
                    .map(item -> item.getProduct().getPrice()
                            .multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            model.addAttribute("cartTotal", cartTotal);
            return "buyer/cart";
        }
        User buyer = getCurrentUser(ud);
        try {
            var order = orderService.checkout(buyer, dto.getShippingAddress());
            ra.addFlashAttribute("success",
                    "Order #" + order.getId() + " placed successfully!");
            return "redirect:/buyer/orders";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/buyer/orders")
    public String orders(@AuthenticationPrincipal UserDetails ud, Model model) {
        User buyer = getCurrentUser(ud);
        model.addAttribute("orders", orderService.findByBuyer(buyer));
        return "buyer/orders";
    }

    @GetMapping("/buyer/orders/{id}")
    public String orderDetail(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails ud,
                              Model model) {
        User buyer = getCurrentUser(ud);
        var order = orderService.findById(id);
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            return "redirect:/buyer/orders";
        }
        model.addAttribute("order", order);
        return "buyer/order-detail";
    }
}
