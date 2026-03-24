package com.root.root.service;

import com.root.root.dto.LoginRequestDto;
import com.root.root.dto.UserInfoDto;
import com.root.root.dto.UserSignupDto;
import com.root.root.entity.User;
import com.root.root.repository.UserRepository;
import com.root.root.util.JwtUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
        String encryptedPassword = passwordEncoder.encode(dto.getLoginPw());
        User user = dto.toEntity(encryptedPassword);

        return userRepository.save(user);
    }

    public String login(LoginRequestDto request){
        User user = userRepository.findByLoginId(request.getLoginId()).orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디입니다."));
        if(!passwordEncoder.matches(request.getLoginPw(), user.getLoginPw())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtUtil.createToken(user.getLoginId());
    }

    @Transactional(readOnly = true)
    public UserInfoDto getMyInfo(String loginId){
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        return UserInfoDto.builder().loginId(user.getLoginId()).name(user.getName()).build();
    }
}
