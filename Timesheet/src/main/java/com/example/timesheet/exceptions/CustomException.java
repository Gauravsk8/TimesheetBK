package com.example.timesheet.exceptions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
@Getter
public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CustomException(String message) {
        super(message);
        if (log.isErrorEnabled()) {
            String stackTrace = ExceptionUtils.getStackTrace(this);
            log.error("Exception Stack Trace: {}", stackTrace);
        }
    }

    public CustomException() {
        super();
        if (log.isErrorEnabled()) {
            String stackTrace = ExceptionUtils.getStackTrace(this);
            log.error("Exception Stack Trace: {}", stackTrace);
        }
    }
}

