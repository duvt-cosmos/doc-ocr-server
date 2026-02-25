package com.example.dococrserver.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
    @GetMapping
    String helloWorld() {
        return "Hello world";
    }
}
