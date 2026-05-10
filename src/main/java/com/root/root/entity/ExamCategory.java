package com.root.root.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examCategoryId; // PK

    @Column(nullable = false)
    private String examCategoryName; // 카테고리 명 (정보기술, 디자인 등)

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ExamCategory parentCategory; // 상위 카테고리 (부모)

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ExamCategory> subCategories = new ArrayList<>(); // 하위 카테고리들 (자식)

    // ExamData와의 연결 (양방향)
    @OneToMany(mappedBy = "examCategory")
    @Builder.Default
    private List<ExamData> exams = new ArrayList<>();
}
