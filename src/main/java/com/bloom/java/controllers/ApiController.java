package com.bloom.java.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bloom.java.service.CalfloraService;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/search")
    public String search(
        @RequestParam(value = "q", required = true) String query
    ) {
        List<Map<String, String>> matches = CalfloraService.search(query);
        return matches.toString();
    }
}