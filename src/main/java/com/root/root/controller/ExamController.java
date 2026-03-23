package com.root.root.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.root.root.entity.ExamData;
import com.root.root.repository.ExamDataRepository;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamDataRepository examDataRepository;

    public ExamController(ExamDataRepository examDataRepository) {
        this.examDataRepository = examDataRepository;
    }

    //  DB 데이터 API
    @GetMapping("/all") 
    public List<ExamData> getAllExams() {
        return examDataRepository.findAll();
    }
}
