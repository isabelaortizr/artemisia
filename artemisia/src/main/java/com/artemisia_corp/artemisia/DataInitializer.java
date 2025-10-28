package com.artemisia_corp.artemisia;

import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.enums.StateEntity;
import com.artemisia_corp.artemisia.entity.enums.UserRole;
import com.artemisia_corp.artemisia.repository.UserRepository;
import com.artemisia_corp.artemisia.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RecommendationService recommendationService;

    @Override
    public void run(String... args) throws Exception {
        init();
    }

    private void init() {
        recommendationService.trainRecommendationModel();
        if (repository.count() == 0) {
            User root = repository.save(User.builder()
                    .name("root")
                    .mail("root@mail.com")
                    .role(UserRole.ADMIN)
                    .status(StateEntity.ACTIVE)
                    .password(passwordEncoder.encode("Abc123**"))
                    .build());
            this.repository.save(root);

        }
    }

}
