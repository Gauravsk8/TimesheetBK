package com.example.common.aop;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.common.config.KeycloakAuthorizationEnforcer;
import com.example.common.constants.errorCode;
import com.example.common.constants.errorMessage;
import com.example.common.exceptions.TimeSheetException;
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

import static com.example.common.constants.errorMessage.UNAUTHORIZED_ACCESS;


@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationAspect.class);

    private final KeycloakAuthorizationEnforcer enforcer;
    private final HttpServletRequest request;

    private static final String KEYCLOAK_PROTECTED = "keycloakProtectedMethods()";


    //Matches any method that is annotated with the @RequiresKeycloakAuthorization
    @Pointcut("@annotation(com.example.common.annotations.RequiresKeycloakAuthorization)")
    public void keycloakProtectedMethods() {}


    //JoinPoint gives you method info like its name, parameters, etc.
    @Before(KEYCLOAK_PROTECTED)
    public void logBefore(JoinPoint joinPoint) {
        logger.info("[BEFORE] Executing method: {}", joinPoint.getSignature());
    }

    @After(KEYCLOAK_PROTECTED)
    public void logAfter(JoinPoint joinPoint) {
        logger.info("[AFTER] Finished method: {}", joinPoint.getSignature());
    }

    @AfterReturning(pointcut = KEYCLOAK_PROTECTED, returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("[AFTER RETURNING] Method: {} returned: {}", joinPoint.getSignature(), result);
    }

    @AfterThrowing(pointcut = KEYCLOAK_PROTECTED, throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        logger.error("[EXCEPTION] Method: {} threw: {}", joinPoint.getSignature(), ex.getMessage(), ex);
    }

    @Around(KEYCLOAK_PROTECTED)
    public Object authorizeAndProceed(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        RequiresKeycloakAuthorization annotation = method.getAnnotation(RequiresKeycloakAuthorization.class);

        String resource = annotation.resource();
        String scope = annotation.scope();

        Object[] args = joinPoint.getArgs();
        String token = null;
        for (Object arg : args) {
            if (arg instanceof String s && s.startsWith("Bearer ")) {
                token = s;
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