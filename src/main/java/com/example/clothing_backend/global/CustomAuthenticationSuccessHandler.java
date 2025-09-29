package com.example.clothing_backend.global;

import com.example.clothing_backend.user.LoginInfo;
import com.example.clothing_backend.user.User;
import com.example.clothing_backend.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 로그인 성공 시 User 꺼내오기
        User user = (User) authentication.getPrincipal();

        // 가벼운 로그인 정보 객체 생성
        LoginInfo loginInfo = new LoginInfo(user.getUserId(), user.getId(), user.getNickname());
        loginInfo.setRoles(userRepository.findRolesByUserId(user.getUserId()));

        // 세션에 저장
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", user);
        session.setAttribute("loginInfo", loginInfo);

        // 요청 구분 (API vs 웹)
        if (request.getRequestURI().startsWith("/api/")) {
            // API 요청 → JSON 응답
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{ \"status\": 200, " +
                            "\"message\": \"로그인 성공\", " +
                            "\"userId\": " + user.getUserId() + ", " +
                            "\"nickname\": \"" + user.getNickname() + "\" }"
            );
        } else {
            // 웹 요청 → 기존처럼 redirect
            response.sendRedirect("/");
        }
    }
}
