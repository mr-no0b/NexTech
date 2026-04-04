package com.nextech.config;

import com.nextech.entity.*;
import com.nextech.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initAdminUser();
        initCategories();
        initSampleData();
        log.info("NexTech data initialisation complete.");
    }

    private void initRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
                log.info("Created role: {}", roleName);
            }
        }
    }

    private void initAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();
            User admin = new User();
            admin.setUsername("admin");
            admin.setFullName("NexTech Admin");
            admin.setEmail("admin@nextech.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
            log.info("Created default admin user (admin / admin123)");
        }
    }

    private void initCategories() {
        String[][] categories = {
            {"Laptops",       "High-performance laptops for work and gaming",         "fa-laptop"},
            {"Processors",    "CPUs from Intel, AMD and ARM",                          "fa-microchip"},
            {"RAM",           "Memory modules for desktops and laptops",               "fa-memory"},
            {"Graphics Cards","GPUs for gaming, design and AI workloads",             "fa-display"},
            {"Monitors",      "4K, ultrawide and gaming displays",                     "fa-desktop"},
            {"Storage",       "SSDs, HDDs and NVMe drives",                           "fa-hard-drive"},
            {"Gaming Chairs", "Ergonomic chairs built for long gaming sessions",       "fa-chair"},
            {"Keyboards",     "Mechanical, membrane and wireless keyboards",           "fa-keyboard"},
            {"Headsets",      "Gaming and studio audio headsets",                     "fa-headphones"},
            {"Mice",          "Gaming and productivity mice",                          "fa-computer-mouse"}
        };
        for (String[] cat : categories) {
            if (!categoryRepository.existsByName(cat[0])) {
                categoryRepository.save(new Category(cat[0], cat[1], cat[2]));
            }
        }
    }

    private void initSampleData() {
        // Only seed if no seller exists yet
        if (userRepository.findByUsername("techseller").isPresent()) return;

        Role sellerRole = roleRepository.findByName(RoleName.ROLE_SELLER).orElseThrow();
        Role buyerRole  = roleRepository.findByName(RoleName.ROLE_BUYER).orElseThrow();

        // Sample seller
        User seller = new User();
        seller.setUsername("techseller");
        seller.setFullName("TechPro Store");
        seller.setEmail("techpro@nextech.com");
        seller.setPassword(passwordEncoder.encode("seller123"));
        seller.setShopName("TechPro Store");
        seller.setEnabled(true);
        seller.setRoles(Set.of(sellerRole));
        userRepository.save(seller);

        // Sample buyer
        User buyer = new User();
        buyer.setUsername("buyer1");
        buyer.setFullName("Alex Johnson");
        buyer.setEmail("alex@nextech.com");
        buyer.setPassword(passwordEncoder.encode("buyer123"));
        buyer.setEnabled(true);
        buyer.setRoles(Set.of(buyerRole));
        userRepository.save(buyer);

        // Sample approved products
        Category laptopCat  = categoryRepository.findByName("Laptops").orElseThrow();
        Category cpuCat     = categoryRepository.findByName("Processors").orElseThrow();
        Category gpuCat     = categoryRepository.findByName("Graphics Cards").orElseThrow();
        Category ramCat     = categoryRepository.findByName("RAM").orElseThrow();
        Category chairCat   = categoryRepository.findByName("Gaming Chairs").orElseThrow();

        createProduct(seller, laptopCat, "ASUS ROG Strix G16",
            "16-inch gaming laptop with RTX 4070, Intel i9, 32GB RAM, 1TB NVMe SSD",
            new BigDecimal("1799.99"), 10, ProductStatus.APPROVED,
            "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=400");

        createProduct(seller, cpuCat, "AMD Ryzen 9 7950X",
            "16-core, 32-thread desktop processor with 5.7GHz max boost",
            new BigDecimal("549.99"), 25, ProductStatus.APPROVED,
            "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?w=400");

        createProduct(seller, gpuCat, "NVIDIA RTX 4090",
            "The ultimate GPU for 4K gaming and AI workloads with 24GB GDDR6X",
            new BigDecimal("1599.99"), 5, ProductStatus.APPROVED,
            "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400");

        createProduct(seller, ramCat, "Corsair Vengeance 32GB DDR5",
            "32GB (2x16GB) DDR5-5600 CL36 high-performance memory kit",
            new BigDecimal("129.99"), 50, ProductStatus.APPROVED,
            "https://images.unsplash.com/photo-1562976540-1502c2145186?w=400");

        createProduct(seller, chairCat, "Secretlab TITAN Evo 2024",
            "Premium gaming chair with lumbar support, 4D armrests and magnetic neck pillow",
            new BigDecimal("449.00"), 15, ProductStatus.APPROVED,
            "https://images.unsplash.com/photo-1587831990711-23ca6441447b?w=400");

        createProduct(seller, laptopCat, "Apple MacBook Pro M3 Max",
            "16-inch MacBook Pro with M3 Max chip, 36GB unified memory, 1TB SSD",
            new BigDecimal("3499.00"), 8, ProductStatus.PENDING, null);

        log.info("Sample data seeded: 1 seller, 1 buyer, 5 approved products, 1 pending.");
    }

    private void createProduct(User seller, Category category, String name,
                                String desc, BigDecimal price, int stock,
                                ProductStatus status, String imageUrl) {
        Product p = new Product();
        p.setSeller(seller);
        p.setCategory(category);
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(price);
        p.setStock(stock);
        p.setStatus(status);
        p.setImageUrl(imageUrl);
        productRepository.save(p);
    }
}
