package com.projects.filestorage.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping(value = {"/auth/sign-in", "/auth/sign-up", "/files/**"})
    public String handleRefresh() {
        return "forward:/index.html";
    }
}