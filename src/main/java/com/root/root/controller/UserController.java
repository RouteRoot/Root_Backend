package com.root.root.controller;

import com.root.root.dto.LoginRequestDto;
import com.root.root.dto.UserInfoDto;
import com.root.root.dto.UserSignupDto;
import com.root.root.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupDto signupDto){
        try{
            userService.registerUser(signupDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto request){
        String token = userService.login(request);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "로그인 성공!");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getMyInfo(Authentication authentication){
        String loginId = authentication.getName();

        UserInfoDto userInfo = userService.getMyInfo(loginId);

        return ResponseEntity.ok(userInfo);
    }
}
