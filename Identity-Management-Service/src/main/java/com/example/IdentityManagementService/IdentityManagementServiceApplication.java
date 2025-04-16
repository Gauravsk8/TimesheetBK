package com.example.IdentityManagementService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableFeignClients(basePackages = "com.example.IdentityManagementService.client") // 👈 This is key!

@EnableAspectJAutoProxy
@SpringBootApplication(scanBasePackages = {
		"com.example.IdentityManagementService",
		"com.example.common"      // shared module's security package
})
public class IdentityManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityManagementServiceApplication.class, args);
	}

}
