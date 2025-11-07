package com.example.demo;

import com.example.demo.domain.userManagementDomain.role.entity.Role;
import com.example.demo.domain.userManagementDomain.role.respository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaAuditing
@EnableAsync
@SpringBootApplication
public class HLineTrunkApplication {

	public static void main(String[] args) {
		SpringApplication.run(HLineTrunkApplication.class, args);
	}


	@Bean
	public CommandLineRunner runner(RoleRepository roleRepository) {
		return args -> {
			if (roleRepository.findByName("USER").isEmpty()) {
				roleRepository.save(Role.builder().name("USER").build());
			}
		};
}


}
