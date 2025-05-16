package com.example.IdentityManagementService.exceptions;

import lombok.Getter;

@Getter
public class KeycloakException extends RuntimeException {
    private final String errorCode;

    public KeycloakException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public KeycloakException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}

