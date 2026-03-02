package com.root.root.controller;

import com.root.root.service.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    private final LLMService llmService;

    @PostMapping("/llm")
    public ResponseEntity<String> testLLM(@RequestBody Map<String, String> request){
        String prompt = request.get("prompt");

        String result = llmService.requestToLlm(prompt);

        return ResponseEntity.ok(result);
    }
}
