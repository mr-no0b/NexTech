package com.nextech.controller;

import com.nextech.dto.ProductDto;
import com.nextech.entity.Product;
import com.nextech.entity.ProductStatus;
import com.nextech.entity.User;
import com.nextech.service.CategoryService;
import com.nextech.service.ProductService;
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
@RequestMapping("/seller")
@PreAuthorize("hasRole('SELLER')")
@RequiredArgsConstructor
public class SellerController {

    private final ProductService productService;
    private final UserService userService;
    private final CategoryService categoryService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User seller = getCurrentUser(userDetails);
        model.addAttribute("seller", seller);
        model.addAttribute("totalProducts",    productService.countBySeller(seller));
        model.addAttribute("pendingProducts",  productService.countBySellerAndStatus(seller, ProductStatus.PENDING));
        model.addAttribute("approvedProducts", productService.countBySellerAndStatus(seller, ProductStatus.APPROVED));
        model.addAttribute("rejectedProducts", productService.countBySellerAndStatus(seller, ProductStatus.REJECTED));
        model.addAttribute("recentProducts",   productService.findBySeller(seller).stream().limit(5).toList());
        return "seller/dashboard";
    }

    @GetMapping("/products")
    public String products(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User seller = getCurrentUser(userDetails);
        model.addAttribute("products", productService.findBySeller(seller));
        return "seller/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("productDto", new ProductDto());
        model.addAttribute("categories", categoryService.findAll());
        return "seller/add-product";
    }

    @PostMapping("/products/add")
    public String addProduct(@Valid @ModelAttribute("productDto") ProductDto dto,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "seller/add-product";
        }
        User seller = getCurrentUser(userDetails);
        productService.addProduct(dto, seller);
        ra.addFlashAttribute("success",
                "Product submitted! It will be visible after admin approval.");
        return "redirect:/seller/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        Product product = productService.findById(id);
        User seller = getCurrentUser(userDetails);
        if (!product.getSeller().getId().equals(seller.getId())) {
            return "redirect:/seller/products";
        }
        ProductDto dto = new ProductDto();
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryId(product.getCategory().getId());

        model.addAttribute("productDto", dto);
        model.addAttribute("productId", id);
        model.addAttribute("categories", categoryService.findAll());
        return "seller/edit-product";
    }

    @PostMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id,
                              @Valid @ModelAttribute("productDto") ProductDto dto,
                              BindingResult result,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model,
                              RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("categories", categoryService.findAll());
            return "seller/edit-product";
        }
        User seller = getCurrentUser(userDetails);
        productService.updateProduct(id, dto, seller);
        ra.addFlashAttribute("success", "Product updated. Pending admin re-approval.");
        return "redirect:/seller/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes ra) {
        User seller = getCurrentUser(userDetails);
        productService.deleteProduct(id, seller);
        ra.addFlashAttribute("success", "Product deleted.");
        return "redirect:/seller/products";
    }
}
