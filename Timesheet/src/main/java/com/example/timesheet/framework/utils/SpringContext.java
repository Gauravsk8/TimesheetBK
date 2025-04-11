package com.example.timesheet.framework.utils;


import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;

    public static <T extends Object> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public void setApplicationContext(ApplicationContext context) {
        SpringContext.context = context;
    }
}