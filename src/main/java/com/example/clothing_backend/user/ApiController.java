package com.example.clothing_backend.user;


import com.example.clothing_backend.user.LoginInfo;
import com.example.clothing_backend.user.User;
import com.example.clothing_backend.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final UserService userservice;
    private final PasswordEncoder passwordEncoder;

    // User API

    @PostMapping("/user/register")
    public Map<String, Object> registerUser(@RequestParam String id,
                                            @RequestParam String password,
                                            @RequestParam String passwordCheck,
                                            @RequestParam String nickname,
                                            @RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        if (!password.equals(passwordCheck)) {
            response.put("status", "error");
            response.put("message", "비밀번호 불일치");
            return response;
        }

        User user = new User();
        user.setId(id);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setEmail(email);

        userservice.addUser(user); // User 객체를 서비스에 넘김

        response.put("status", "success");
        response.put("message", "회원가입 성공");
        return response;
    }

    @PostMapping("/user/login")
    public Map<String, Object> loginUser(@RequestParam String id,
                                         @RequestParam String password,
                                         HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = userservice.getUser(id);
        if (user == null) {
            response.put("status", "error");
            response.put("message", "사용자를 찾을 수 없습니다.");
            return response;
        }
        
        // BCrypt 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("status", "error");
            response.put("message", "비밀번호가 일치하지 않습니다.");
            return response;
        }

        // 프로필 이미지 Base64 세팅
        String base64Image = userservice.getProfileImageBase64(user.getId());
        if (base64Image != null) user.setProfileImageBase64(base64Image);

        session.setAttribute("loginUser", user);

        // 로그인 정보 세션에 저장 (권한 포함)
        LoginInfo loginInfo = new LoginInfo(user.getUserId(), user.getId(), user.getNickname());
        loginInfo.setRoles(userservice.getRoles(user.getUserId()));
        session.setAttribute("loginInfo", loginInfo);

        response.put("status", "success");
        response.put("message", "로그인 성공");
        response.put("userId", user.getId());
        response.put("nickname", user.getNickname());
        return response;
    }

    @PostMapping("/user/logout")
    public Map<String, Object> logoutUser(HttpSession session) {
        session.invalidate(); // 세션 삭제
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "로그아웃 완료");
        return response;
    }

    // MyPage API

    @CrossOrigin(
            origins = {"http://localhost:5173"},      allowCredentials = "true"
    ) // 도메인 허용 쿠키 허용 쿠키허용 추가

    @PostMapping("/mypage/uploadProfile")
    public Map<String, Object> uploadProfile(@RequestParam MultipartFile profileImage, HttpSession session) throws IOException {
        Map<String, Object> response = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("status", "error");
            response.put("message", "로그인 필요");
            return response;
        }

        // 이미지 bytes를 Base64로 변환 후 User에 세팅
        String base64 = userservice.saveProfileImage(profileImage, loginUser.getId());
        loginUser.setProfileImageBase64(base64);
        session.setAttribute("loginUser", loginUser);

        response.put("status", "success");
        response.put("message", "프로필 업로드 성공");
        return response;
    }

    @PostMapping("/mypage/resetPassword")
    public Map<String, Object> resetPassword(@RequestParam String newPassword,
                                             @RequestParam String newPasswordCheck,
                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("status", "error");
            response.put("message", "로그인 필요");
            return response;
        }
        if (!newPassword.equals(newPasswordCheck)) {
            response.put("status", "error");
            response.put("message", "비밀번호 불일치");
            return response;
        }

        userservice.updatePassword(loginUser.getId(), loginUser.getEmail(), newPassword);
        session.invalidate(); // 비밀번호 변경 후 세션 초기화

        response.put("status", "success");
        response.put("message", "비밀번호 변경 성공");
        return response;
    }

    @PostMapping("/mypage/deleteAccount")
    public Map<String, Object> deleteAccount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.put("status", "error");
            response.put("message", "로그인 필요");
            return response;
        }

        userservice.deleteUser(loginUser.getId());
        session.invalidate(); // 탈퇴 후 세션 초기화

        response.put("status", "success");
        response.put("message", "회원 탈퇴 성공");
        return response;
    }
}