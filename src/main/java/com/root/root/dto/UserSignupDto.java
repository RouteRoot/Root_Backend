package com.root.root.dto;

import com.root.root.entity.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter@NoArgsConstructor
public class UserSignupDto {
    private String loginId;
    private String loginPw;
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String phoneNumber;

    public User toEntity(String encryptedPassword){
        User user = new User();
        user.setLoginId(this.loginId);
        user.setLoginPw(encryptedPassword);
        user.setName(this.name);
        user.setNickname(this.nickname);
        user.setBirthDate(this.birthDate);
        user.setPhoneNumber(this.phoneNumber);
        return user;
    }
}
