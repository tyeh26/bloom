package com.bloom.java.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bloom.java.service.CalfloraService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class ApiController {


    // Given a query, return a scraped list of Calflora binomials
    @GetMapping("/search")
    public String search(
        @RequestParam(value = "q", required = true) String query
    ) { 
        List<Map<String, String>> matches = CalfloraService.search(query);
        ObjectMapper objectMapper = new ObjectMapper();

        String json;

        try {
            Map<String, Object> resultMap = Map.of("result", matches);
            json = objectMapper.writeValueAsString(resultMap);
        } catch (JsonProcessingException e) {
            return "{\"result\":[]}";
        }
        return json;
    }

    // Given a Calflora crn, return a list of months when the plant is in bloom
    @GetMapping("/blooms")
    public String blooms(
        @RequestParam(value = "crn", required = true) String crn) {
            List<Integer> res = CalfloraService.getBlooms(crn);
            return res.toString();
    }
}