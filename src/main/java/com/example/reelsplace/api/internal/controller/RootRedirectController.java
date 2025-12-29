package com.example.reelsplace.api.internal.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {

    /**
     * 루트 접속 시 데모 페이지로 리다이렉트
     * Meta 검수용
     */
    @GetMapping("/")
    public String redirectToDemo() {
        return "redirect:/new_test.html";
    }
}