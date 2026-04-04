package com.nextech.service;

import com.nextech.dto.ProductDto;
import com.nextech.entity.Product;
import com.nextech.entity.ProductStatus;
import com.nextech.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Product addProduct(ProductDto dto, User seller);
    Product updateProduct(Long id, ProductDto dto, User seller);
    Product findById(Long id);
    List<Product> findBySeller(User seller);
    List<Product> findByStatus(ProductStatus status);
    Page<Product> findApprovedFiltered(Long categoryId, String search, Pageable pageable);
    Product approveProduct(Long id);
    Product rejectProduct(Long id, String reason);
    void deleteProduct(Long id, User requestingUser);
    long countByStatus(ProductStatus status);
    long countBySeller(User seller);
    long countBySellerAndStatus(User seller, ProductStatus status);
}
