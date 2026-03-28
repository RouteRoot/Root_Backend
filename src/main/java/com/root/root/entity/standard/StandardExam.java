package com.root.root.entity.standard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "standard_exam")
public class StandardExam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String certificationName;

    @Column(columnDefinition = "TEXT")
    private String eligibilityCondition;

    @OneToMany(mappedBy = "standardExam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StandardExamSyllabus> syllabuses = new ArrayList<>();
}
