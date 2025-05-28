package com.artemisia_corp.artemisia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableJpaRepositories(basePackages = "com.artemisia_corp.artemisia.repository")
@EntityScan("com.artemisia_corp.artemisia.entity")
@SpringBootApplication(scanBasePackages = "com.artemisia_corp.artemisia")
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
@EnableAsync
public class ArtemisiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArtemisiaApplication.class, args);
	}

}
