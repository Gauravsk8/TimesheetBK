// src/test/java/com/example/timesheet/SecuredTestController.java
package com.example.timesheet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecuredTestController {

    @GetMapping("/secured")
    public String securedEndpoint() {
        return "This is a secured endpoint";
    }
}
