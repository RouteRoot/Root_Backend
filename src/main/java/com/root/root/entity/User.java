package com.root.root.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    @Column
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

    // 로드맵 생성 시 업데이트될 추가 정보들
    // 회원가입 시 입력 X, 로드맵 생성 시 입력
    private String major;
    private String hope;
    private String acquired;
    private String status;
    private int daily, weekly;
    private String mylevel;
    private String target;
}
