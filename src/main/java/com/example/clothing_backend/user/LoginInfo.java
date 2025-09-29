package com.example.clothing_backend.user;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class LoginInfo {

    private Long userId;      // DB상 유저 고유 번호
    private String id;        // 로그인용 아이디
    private String nickname;  // 화면에 표시할 닉네임
    private List<String> roles; // 권한 목록 (ROLE_USER, ROLE_ADMIN 등)

    public LoginInfo(Long userId, String id, String nickname) {
        this.userId = userId;
        this.id = id;
        this.nickname = nickname;
    }
}