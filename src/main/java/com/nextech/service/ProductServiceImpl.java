package com.nextech.service;

import com.nextech.dto.ProductDto;
import com.nextech.entity.*;
import com.nextech.exception.ResourceNotFoundException;
import com.nextech.repository.CategoryRepository;
import com.nextech.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Product addProduct(ProductDto dto, User seller) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", dto.getCategoryId()));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(category);
        product.setSeller(seller);
        product.setStatus(ProductStatus.PENDING);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductDto dto, User seller) {
        Product product = findById(id);
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new IllegalArgumentException("You are not authorized to edit this product");
        }
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", dto.getCategoryId()));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(category);
        // Reset to PENDING after edit so admin must re-approve
        product.setStatus(ProductStatus.PENDING);
        product.setRejectionReason(null);
        return productRepository.save(product);
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Override
    public List<Product> findBySeller(User seller) {
        return productRepository.findBySeller(seller);
    }

    @Override
    public List<Product> findByStatus(ProductStatus status) {
        return productRepository.findByStatus(status);
    }

    @Override
    public Page<Product> findApprovedFiltered(Long categoryId, String search, Pageable pageable) {
        String searchPattern = (search == null || search.isBlank()) ? null : "%" + search.trim().toLowerCase() + "%";
        Long catParam = (categoryId == null || categoryId == 0) ? null : categoryId;
        return productRepository.findApprovedFiltered(catParam, searchPattern, pageable);
    }

    @Override
    @Transactional
    public Product approveProduct(Long id) {
        Product product = findById(id);
        product.setStatus(ProductStatus.APPROVED);
        product.setRejectionReason(null);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product rejectProduct(Long id, String reason) {
        Product product = findById(id);
        product.setStatus(ProductStatus.REJECTED);
        product.setRejectionReason(reason);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, User requestingUser) {
        Product product = findById(id);
        boolean isAdmin = requestingUser.hasRole(RoleName.ROLE_ADMIN);
        boolean isOwner = product.getSeller().getId().equals(requestingUser.getId());
        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("You are not authorized to delete this product");
        }
        productRepository.delete(product);
    }

    @Override
    public long countByStatus(ProductStatus status) {
        return productRepository.countByStatus(status);
    }

    @Override
    public long countBySeller(User seller) {
        return productRepository.countBySeller(seller);
    }

    @Override
    public long countBySellerAndStatus(User seller, ProductStatus status) {
        return productRepository.countBySellerAndStatus(seller, status);
    }
}
