package com.root.root.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 고유 식별자

    @Column(nullable = false, unique = true, length = 50)
    private String loginId; // 회원 아이디

    @Column(length = 255)
    private String loginPw; // 회원 비밀번호

    @Column(nullable = false, length = 50)
    private String name; // 이름

    @Column(nullable = false, unique = true, length = 50)
    private String nickname; // 닉네임 (중복 방지)

    @Column(nullable = false)
    private LocalDate birthDate; // 생년월일(0000-00-00)

    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber; // 전화번호(000-0000-0000)

    // OAuth 필드
    private String provider;
    private String providerId;

    @Column(nullable = false)
    private boolean isOnboardingCompleted = false; // 온보딩 완료 여부

    @Column
    private int totalExp = 0; // 누적 경험치(배지 시스템용)

    // 로드맵 생성 시 업데이트될 추가 정보들
    // 회원가입 시 입력 X, 로드맵 생성 시 입력
    @Column
    private String educationStatus;

    @Column
    private int grade;

    @Column
    private String major;

    @Column
    private String hope;

    @Column
    private boolean isMajorRelated;

    @Column
    private int career;

    //@Column
    //private int daily;

    //@Column
    //private int weekly;

    @Column
    private String mylevel;

    @Column
    private String target;

    @Column(columnDefinition = "TEXT")
    private String personalStory;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_acquired_certs", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "certification_name")
    private List<String> acquired = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Roadmap roadmap;
}
