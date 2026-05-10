package com.root.root.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @JsonIgnore
    @Builder.Default
    private List<ExamData> exams = new ArrayList<>();
}
