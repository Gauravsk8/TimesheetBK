// src/test/java/com/example/timesheet/TestExceptionController.java
package com.example.timesheet;

import com.example.timesheet.constants.errorCode;
import com.example.timesheet.exceptions.TimeSheetException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.ConstraintViolationException;

@RestController
@RequestMapping("/test")
@Validated
public class TestController {

    @GetMapping("/timesheet-exception")
    public void throwTimeSheetException() {
        throw new TimeSheetException(errorCode.NOT_FOUND_ERROR, "Test TimeSheetException");
    }

    @PostMapping("/validation-exception")
    public void throwValidationException(@RequestParam @NotBlank @Size(min = 5) String name) {
        // @Valid param throws MethodArgumentNotValidException
    }

    @GetMapping("/constraint-violation")
    public void throwConstraintViolation() {
        throw new ConstraintViolationException("Invalid parameter", null);
    }

    @GetMapping("/data-conflict")
    public void throwDataConflict() {
        throw new DataIntegrityViolationException("Duplicate key");
    }

    @GetMapping("/security")
    public void throwSecurity() {
        throw new SecurityException("Forbidden");
    }

    @GetMapping("/general")
    public void throwGeneral() {
        throw new RuntimeException("General error");
    }
}
