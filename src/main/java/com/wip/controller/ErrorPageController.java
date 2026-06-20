package com.wip.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ErrorPageController Component.
 * 
 * Handles operations and data related to ErrorPageController.
 */
@Controller
public class ErrorPageController {

    @GetMapping("/access-denied")
    public String accessDeniedPage() {
        return "error/access-denied";
    }
}
