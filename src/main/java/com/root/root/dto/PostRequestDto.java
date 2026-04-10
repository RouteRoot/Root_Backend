package com.root.root.dto;

import com.root.root.entity.BoardType;
import com.root.root.entity.StudyStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDto {
    private String title;
    private String content;
    private Long userId;
    private BoardType boardType;
    private String category;
    private StudyStatus studyStatus;
}