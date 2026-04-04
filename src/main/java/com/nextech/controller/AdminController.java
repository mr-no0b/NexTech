package com.nextech.controller;

import com.nextech.entity.*;
import com.nextech.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final CategoryService categoryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProducts",   productService.countByStatus(ProductStatus.APPROVED));
        model.addAttribute("pendingProducts", productService.countByStatus(ProductStatus.PENDING));
        model.addAttribute("totalUsers",      userService.findAll().size());
        model.addAttribute("totalOrders",     orderService.findAll().size());
        model.addAttribute("pendingList",     productService.findByStatus(ProductStatus.PENDING));
        return "admin/dashboard";
    }

    /* ─── Product Management ────────────────────────────────────────────── */

    @GetMapping("/products")
    public String products(Model model,
                           @RequestParam(defaultValue = "ALL") String status) {
        List<Product> products = switch (status) {
            case "PENDING"  -> productService.findByStatus(ProductStatus.PENDING);
            case "APPROVED" -> productService.findByStatus(ProductStatus.APPROVED);
            case "REJECTED" -> productService.findByStatus(ProductStatus.REJECTED);
            default         -> productService.findByStatus(ProductStatus.PENDING);
        };
        model.addAttribute("products", products);
        model.addAttribute("currentStatus", status);
        return "admin/products";
    }

    @PostMapping("/products/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        productService.approveProduct(id);
        ra.addFlashAttribute("success", "Product approved successfully.");
        return "redirect:/admin/products?status=PENDING";
    }

    @PostMapping("/products/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String reason,
                         RedirectAttributes ra) {
        String rejectionReason = (reason == null || reason.isBlank())
                ? "Rejected by admin" : reason;
        productService.rejectProduct(id, rejectionReason);
        ra.addFlashAttribute("success", "Product rejected.");
        return "redirect:/admin/products?status=PENDING";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        User admin = userService.findAll().stream()
                .filter(u -> u.hasRole(RoleName.ROLE_ADMIN))
                .findFirst().orElseThrow();
        productService.deleteProduct(id, admin);
        ra.addFlashAttribute("success", "Product deleted.");
        return "redirect:/admin/products?status=PENDING";
    }

    /* ─── User Management ───────────────────────────────────────────────── */

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        User user = userService.findById(id);
        if (user.hasRole(RoleName.ROLE_ADMIN)) {
            ra.addFlashAttribute("error", "Cannot disable admin account.");
            return "redirect:/admin/users";
        }
        user.setEnabled(!user.isEnabled());
        userService.save(user);
        ra.addFlashAttribute("success",
                "User " + (user.isEnabled() ? "enabled" : "disabled") + ".");
        return "redirect:/admin/users";
    }

    /* ─── Order Management ──────────────────────────────────────────────── */

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderService.findAll());
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    RedirectAttributes ra) {
        orderService.updateStatus(id, OrderStatus.valueOf(status));
        ra.addFlashAttribute("success", "Order status updated.");
        return "redirect:/admin/orders";
    }
}
