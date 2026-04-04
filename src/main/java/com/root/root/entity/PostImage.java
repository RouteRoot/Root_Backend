package com.root.root.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_images")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false)
    private String imageUrl;// 저장된 파일 경로 (ex: /images/uuid.jpg)

    private int uploadOrder;// 이미지 첨부 순서 (1~5)

    @Builder
    public PostImage(Post post, String imageUrl, int uploadOrder) {
        this.post = post;
        this.imageUrl = imageUrl;
        this.uploadOrder = uploadOrder;
    }
}