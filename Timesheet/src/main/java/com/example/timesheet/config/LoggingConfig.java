package com.example.timesheet.config;

import com.example.common.logging.MDCLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class LoggingConfig {

    @Bean
    public FilterRegistrationBean<MDCLoggingFilter> loggingFilter() {
        FilterRegistrationBean<MDCLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new MDCLoggingFilter());
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
