package com.upb.modulo_01;

import com.upb.modulo_01.entity.MyUser;
import com.upb.modulo_01.entity.enums.RoleType;
import com.upb.modulo_01.entity.enums.StateEntity;
import com.upb.modulo_01.repository.UserRepository;
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
            MyUser root = repository.save(MyUser.builder()
                    .username("root")
                    .name("root")
                    .lastName("Root")
                    .documentNumber("777777")
                    .role(RoleType.ROLE_ADMIN)
                    .status(StateEntity.ACTIVE)
                    .password(passwordEncoder.encode("Abc123**"))
                    .build());
            this.repository.save(root);

        }
    }

}
