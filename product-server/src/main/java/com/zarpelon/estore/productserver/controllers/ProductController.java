package com.zarpelon.estore.productserver.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {
    @GetMapping
    public String get() {
        return "GET Products";
    }
}
