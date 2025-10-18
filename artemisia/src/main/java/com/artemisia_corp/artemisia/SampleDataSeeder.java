package com.artemisia_corp.artemisia;

import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.UserPreference;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import com.artemisia_corp.artemisia.entity.enums.ProductStatus;
import com.artemisia_corp.artemisia.entity.enums.StateEntity;
import com.artemisia_corp.artemisia.entity.enums.UserRole;
import com.artemisia_corp.artemisia.repository.ProductRepository;
import com.artemisia_corp.artemisia.repository.UserPreferenceRepository;
import com.artemisia_corp.artemisia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.*;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = false)
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class SampleDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Create 5 sellers with 10 products each if products table is empty
            if (productRepository.count() == 0) {
                List<User> sellers = new ArrayList<>();
                for (int s = 1; s <= 5; s++) {
                    User seller = User.builder()
                            .name("seller" + s)
                            .mail("seller" + s + "@example.com")
                            .password(passwordEncoder.encode("Seller123*"))
                            .role(UserRole.SELLER)
                            .status(StateEntity.ACTIVE)
                            .build();
                    seller = userRepository.save(seller);
                    sellers.add(seller);
                }

                // Categories and techniques pools to distribute
                PaintingCategory[] cats = PaintingCategory.values();
                PaintingTechnique[] techs = PaintingTechnique.values();

                int productCounter = 1;
                for (User seller : sellers) {
                    for (int i = 0; i < 10; i++) {
                        // pick category and technique in a rotating manner
                        PaintingCategory cat = cats[(productCounter - 1) % cats.length];
                        PaintingTechnique tech = techs[(productCounter - 1) % techs.length];

                        Product p = Product.builder()
                                .seller(seller)
                                .name(String.format("Obra %03d - %s", productCounter, seller.getName()))
                                .materials("Lienzo y pigmento")
                                .description("Obra de muestra creada para pruebas - " + productCounter)
                                .price(50.0 + (productCounter * 30.0))
                                .stock(1 + (productCounter % 5))
                                .status(ProductStatus.AVAILABLE)
                                .imageUrl(null)
                                .techniques(new HashSet<>(Collections.singletonList(tech)))
                                .categories(new HashSet<>(Collections.singletonList(cat)))
                                .build();

                        productRepository.save(p);
                        productCounter++;
                    }
                }

                log.info("Seeded {} sellers with products", sellers.size());
            } else {
                log.info("Products already present, skipping seeder");
            }

            // Create/Update root user preference vector
            Optional<User> rootOpt = userRepository.findAll().stream()
                    .filter(u -> "root".equals(u.getName()))
                    .findFirst();

            if (rootOpt.isPresent()) {
                User root = rootOpt.get();

                // Prepare a preference vector favoring several categories and techniques
                Map<String, Double> pref = new HashMap<>();
                // Category weights (favor Abstracta, Contemporánea, Expresionista)
                pref.put("cat_Abstracta", 1.0);
                pref.put("cat_Contemporánea", 0.8);
                pref.put("cat_Expresionista", 0.6);
                pref.put("cat_Realista", 0.2);

                // Technique weights (favor Óleo and Acrílico)
                pref.put("tech_Óleo", 0.9);
                pref.put("tech_Acrílico", 0.7);

                // Derived features
                pref.put("price_sensitivity", 0.2);
                pref.put("style_preference", 0.8);
                pref.put("color_intensity", 0.9);
                pref.put("modern_traditional", 0.7);
                pref.put("complexity_preference", 0.5);

                // Normalize (simple L1 norm)
                double sum = pref.values().stream().mapToDouble(Double::doubleValue).sum();
                if (sum > 0) {
                    Map<String, Double> normalized = new HashMap<>();
                    pref.forEach((k, v) -> normalized.put(k, v / sum));
                    pref = normalized;
                }

                // Save into user preferences table
                UserPreference up = userPreferenceRepository.findByUser(root).orElse(null);
                if (up == null) {
                    up = UserPreference.builder()
                            .user(root)
                            .preferenceVector(pref)
                            .build();
                } else {
                    up.setPreferenceVector(pref);
                }
                userPreferenceRepository.save(up);
                log.info("Root user preferences seeded/updated for user id={}", root.getId());
            } else {
                log.warn("Root user not found; skipping root preference seeding");
            }

        } catch (Exception e) {
            log.error("Error during sample data seeding: {}", e.getMessage(), e);
        }
    }
}
