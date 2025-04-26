package com.opnhire.UserBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"entity"})
@EnableJpaRepositories(basePackages = {"Repository"})
@ComponentScan(basePackages = {"controller", "service", "config", "util", "com.opnhire.UserBackend"})
public class UserBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserBackendApplication.class, args);
	}

}
