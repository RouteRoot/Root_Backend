package com.root.root.dto;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamCategoryResponseDto {

    private Long examCategoryId;
    private String examCategoryName;
    private Long examCount; // 자격증 개수 (자식들 개수 포함 합계)
    private List<ExamCategoryResponseDto> subCategories; // 하위 카테고리 목록
}
