package com.nextech.controller;

import com.nextech.service.CategoryService;
import com.nextech.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/products")
    public String productList(@RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) String search,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        int pageSize = 12;
        var productsPage = productService.findApprovedFiltered(categoryId, search,
                PageRequest.of(page, pageSize, Sort.by("createdAt").descending()));

        model.addAttribute("products",   productsPage.getContent());
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("search", search);
        return "product/list";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "product/detail";
    }
}
