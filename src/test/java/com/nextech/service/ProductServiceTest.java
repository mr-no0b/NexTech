package com.nextech.service;

import com.nextech.dto.ProductDto;
import com.nextech.entity.*;
import com.nextech.exception.ResourceNotFoundException;
import com.nextech.repository.CategoryRepository;
import com.nextech.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;

    @InjectMocks ProductServiceImpl productService;

    private Category category;
    private User seller;
    private Product product;
    private ProductDto dto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Laptops");

        seller = new User();
        seller.setUsername("techseller");

        product = new Product();
        product.setName("Gaming Laptop");
        product.setPrice(new BigDecimal("1299.99"));
        product.setStock(10);
        product.setCategory(category);
        product.setSeller(seller);
        product.setStatus(ProductStatus.PENDING);

        dto = new ProductDto();
        dto.setName("Gaming Laptop");
        dto.setDescription("A fast laptop");
        dto.setPrice(new BigDecimal("1299.99"));
        dto.setStock(10);
        dto.setCategoryId(1L);
    }

    @Test
    void addProduct_setsPendingStatus() {
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));

        Product result = productService.addProduct(dto, seller);

        assertThat(result.getStatus()).isEqualTo(ProductStatus.PENDING);
        assertThat(result.getSeller()).isEqualTo(seller);
        assertThat(result.getCategory()).isEqualTo(category);
    }

    @Test
    void findById_returnsProduct() {
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        Product result = productService.findById(1L);

        assertThat(result.getName()).isEqualTo("Gaming Laptop");
    }

    @Test
    void findByStatus_returnsPendingProducts() {
        given(productRepository.findByStatus(ProductStatus.PENDING)).willReturn(List.of(product));

        List<Product> results = productService.findByStatus(ProductStatus.PENDING);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(ProductStatus.PENDING);
    }

    @Test
    void approveProduct_changesStatusToApproved() {
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        Product result = productService.approveProduct(1L);

        assertThat(result.getStatus()).isEqualTo(ProductStatus.APPROVED);
        assertThat(result.getRejectionReason()).isNull();
    }

    @Test
    void rejectProduct_setsRejectedStatusWithReason() {
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(productRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        Product result = productService.rejectProduct(1L, "Price too high");

        assertThat(result.getStatus()).isEqualTo(ProductStatus.REJECTED);
        assertThat(result.getRejectionReason()).isEqualTo("Price too high");
    }
}
