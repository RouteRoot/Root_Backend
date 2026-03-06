package com.root.root.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.token}")
    private String secretKey;

    private Key key;
    private final long tokenValidTime = 1000L * 60 * 60 * 24;
    // 암호화 알고리즘(HS256) 세팅
    @PostConstruct
    protected void init(){
        byte[] bytes = Base64.getEncoder().encode(secretKey.getBytes());
        this.key = Keys.hmacShaKeyFor(bytes);
    }
    // 유저 아이디로 JWT 토큰 생성
    public String createToken(String username){
        Date now = new Date();
        return Jwts.builder().setSubject(username).setIssuedAt(now).setExpiration(new Date(now.getTime() + tokenValidTime)).signWith(key, SignatureAlgorithm.HS256).compact();
    }
    // 토큰에서 아이디 추출
    public String getUsernameFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }
    // 토큰 유효성 확인
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }catch(Exception e){
            return false;
        }
    }
}
