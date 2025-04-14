// src/test/java/com/example/timesheet/ExceptionHandlerTest.java
package com.example.timesheet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(NoSecurityConfig.class)
class ExceptionHandlerTest {
    private static final String JSON_PATH_ERROR_CODE = "$.error_code";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testTimeSheetException() throws Exception {
        mockMvc.perform(get("/test/timesheet-exception"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(JSON_PATH_ERROR_CODE).value("TIMESHEET_NOT_FOUND_ERROR"));
    }

    @Test
    void testValidationException() throws Exception {
        mockMvc.perform(post("/test/validation-exception")
                        .param("name", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(JSON_PATH_ERROR_CODE).value("TIMESHEET_VALIDATION_ERROR"));
    }

    @Test
    void testConstraintViolationException() throws Exception {
        mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(JSON_PATH_ERROR_CODE).value("TIMESHEET_VALIDATION_ERROR"));
    }

    @Test
    void testDataConflictException() throws Exception {
        mockMvc.perform(get("/test/data-conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath(JSON_PATH_ERROR_CODE).value("TIMESHEET_CONFLICT_ERROR"));
    }

    @Test
    void testSecurityException() throws Exception {
        mockMvc.perform(get("/test/security"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath(JSON_PATH_ERROR_CODE).value("TIMESHEET_FORBIDDEN_ERROR"));
    }

    @Test
    void testGeneralException() throws Exception {
        mockMvc.perform(get("/test/general"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath(JSON_PATH_ERROR_CODE).value("TIMESHEET_INTERNAL_SERVER_ERROR"));
    }
}
