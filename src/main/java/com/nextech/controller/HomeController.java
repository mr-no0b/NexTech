package com.nextech.controller;

import com.nextech.entity.ProductStatus;
import com.nextech.service.CategoryService;
import com.nextech.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProducts",
            productService.findApprovedFiltered(null, null,
                PageRequest.of(0, 8, Sort.by("createdAt").descending())).getContent());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("totalProducts", productService.countByStatus(ProductStatus.APPROVED));
        return "home";
    }

    @GetMapping("/error/403")
    public String forbidden(Model model) {
        model.addAttribute("errorCode", 403);
        model.addAttribute("errorMessage", "You do not have permission to access this page.");
        return "error/error";
    }
}
