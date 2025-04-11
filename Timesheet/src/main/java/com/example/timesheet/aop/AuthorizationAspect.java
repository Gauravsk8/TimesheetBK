package com.example.timesheet.aop;

import com.example.timesheet.annotations.RequiresKeycloakAuthorization;
import com.example.timesheet.config.KeycloakAuthorizationEnforcer;
import com.example.timesheet.constants.errorCode;
import com.example.timesheet.constants.errorMessage;
import com.example.timesheet.exceptions.TimeSheetException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import static com.example.timesheet.constants.errorMessage.MISSING_BEARER_TOKEN;
import static com.example.timesheet.constants.errorMessage.UNAUTHORIZED_ACCESS;


@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationAspect.class);

    private final KeycloakAuthorizationEnforcer enforcer;
    private final HttpServletRequest request;

    //Matches any method that is annotated with the @RequiresKeycloakAuthorization
    @Pointcut("@annotation(com.example.timesheet.annotations.RequiresKeycloakAuthorization)")
    public void keycloakProtectedMethods() {}


    //JoinPoint gives you method info like its name, parameters, etc.
    @Before("keycloakProtectedMethods()")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("[BEFORE] Executing method: {}", joinPoint.getSignature());
    }

    @After("keycloakProtectedMethods()")
    public void logAfter(JoinPoint joinPoint) {
        logger.info("[AFTER] Finished method: {}", joinPoint.getSignature());
    }

    @AfterReturning(pointcut = "keycloakProtectedMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("[AFTER RETURNING] Method: {} returned: {}", joinPoint.getSignature(), result);
    }

    @AfterThrowing(pointcut = "keycloakProtectedMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        logger.error("[EXCEPTION] Method: {} threw: {}", joinPoint.getSignature(), ex.getMessage(), ex);
    }

    @Around("keycloakProtectedMethods()")
    public Object authorizeAndProceed(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        RequiresKeycloakAuthorization annotation = method.getAnnotation(RequiresKeycloakAuthorization.class);

        String resource = annotation.resource();
        String scope = annotation.scope();

        Object[] args = joinPoint.getArgs();
        String token = null;
        for (Object arg : args) {
            if (arg instanceof String && ((String) arg).startsWith("Bearer ")) {
                token = (String) arg;
                break;
            }
        }

        if (token == null) {
            throw new TimeSheetException(errorCode.MissingBearerToken, errorMessage.MISSING_BEARER_TOKEN);
        }

        boolean authorized = enforcer.isAuthorized(token, resource, scope);
        if (!authorized) {
            throw new SecurityException(UNAUTHORIZED_ACCESS);

        }

        logger.info("Authorization granted. Proceeding with method: {}", joinPoint.getSignature());
        return joinPoint.proceed();
    }
}
