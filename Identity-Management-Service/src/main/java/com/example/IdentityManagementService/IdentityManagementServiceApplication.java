package com.example.IdentityManagementService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

@EnableFeignClients(basePackages = "com.example.IdentityManagementService.client") // 👈 This is key!

@SpringBootApplication
public class IdentityManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityManagementServiceApplication.class, args);
	}

}
