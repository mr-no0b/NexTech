package com.nextech.repository;

import com.nextech.entity.Product;
import com.nextech.entity.ProductStatus;
import com.nextech.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStatus(ProductStatus status);

    List<Product> findBySeller(User seller);

    List<Product> findBySellerAndStatus(User seller, ProductStatus status);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:searchPattern IS NULL OR LOWER(p.name) LIKE :searchPattern)")
    Page<Product> findApprovedFiltered(@Param("categoryId") Long categoryId,
                                       @Param("searchPattern") String searchPattern,
                                       Pageable pageable);

    long countByStatus(ProductStatus status);

    long countBySeller(User seller);

    long countBySellerAndStatus(User seller, ProductStatus status);
}
