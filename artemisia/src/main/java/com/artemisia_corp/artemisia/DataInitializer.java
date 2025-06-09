package com.artemisia_corp.artemisia;

import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.enums.StateEntity;
import com.artemisia_corp.artemisia.entity.enums.UserRole;
import com.artemisia_corp.artemisia.repository.UserRepository;
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

    @Override
    public void run(String... args) throws Exception {
        init();
    }

    private void init() {
        if (repository.count() == 0) {
            User root = repository.save(User.builder()
                    .name("root")
                    .mail("")
                    .role(UserRole.ADMIN)
                    .status(StateEntity.ACTIVE)
                    .password(passwordEncoder.encode("Abc123**"))
                    .build());
            this.repository.save(root);

        }
    }

}
