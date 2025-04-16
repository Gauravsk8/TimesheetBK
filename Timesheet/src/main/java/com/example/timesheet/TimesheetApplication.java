package com.example.timesheet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy
@EnableFeignClients(basePackages = "com.example.Timesheet.client")
@SpringBootApplication(scanBasePackages = {
		"com.example.timesheet",
		"com.example.common"      // shared module's security package
})
@EnableScheduling
public class TimesheetApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimesheetApplication.class, args);
	}

}
