package com.root.root.service;

import com.root.root.dto.UserSignupDto;
import com.root.root.entity.User;
import com.root.root.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User registerUser(UserSignupDto dto){
        // 중복 검사
        if(userRepository.existsByLoginId(dto.getLoginId())){
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if(userRepository.existsByNickname(dto.getNickname())){
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
        // PW 암호화(Spring Security 붙여야 됨)
        String encryptedPassword = dto.getPassword();
        User user = dto.toEntity(encryptedPassword);

        return userRepository.save(user);
    }
}
