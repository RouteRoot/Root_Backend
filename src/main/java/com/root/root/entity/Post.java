package com.root.root.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType;

    private String category;

    @Enumerated(EnumType.STRING)
    private StudyStatus studyStatus;

    private int viewCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostScrap> postScraps = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> postImages = new ArrayList<>();

    @Builder
    public Post(String title, String content, User author, BoardType boardType,
                String category, StudyStatus studyStatus) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.boardType = boardType;
        this.category = category;
        this.studyStatus = (boardType == BoardType.STUDY) ? studyStatus : null;
        this.createdAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void update(String title, String content, String category, StudyStatus studyStatus) {
        this.title = title;
        this.content = content;
        this.category = category;
        if (this.boardType == BoardType.STUDY) {
            this.studyStatus = studyStatus;
        }
        this.updatedAt = LocalDateTime.now();
    }
}