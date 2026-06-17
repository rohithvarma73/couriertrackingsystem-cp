package com.wip.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/access-denied")
    public String accessDeniedPage() {
        return "error/access-denied";
    }
}