package com.example.timesheet.exceptions;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@Slf4j
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidClientDataException extends CustomException {
    private static final long serialVersionUID = 1L;

    public InvalidClientDataException(String message) {
        super(message);
        if (log.isErrorEnabled()) {
            log.error("EntityAlreadyExistsException: {}", message);
        }
    }

    public InvalidClientDataException() {
        super();
    }
}
