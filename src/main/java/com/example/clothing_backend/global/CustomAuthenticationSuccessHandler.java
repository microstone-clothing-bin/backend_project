package com.example.clothing_backend.global;

import com.example.clothing_backend.user.User;
import com.example.clothing_backend.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
// 다시 SavedRequestAwareAuthenticationSuccessHandler를 상속받아 리다이렉트 기능을 활용합니다.
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 사용자 정보 조회 및 세션 저장
        String username = authentication.getName();
        User user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalStateException("인증 성공 후 사용자 정보를 찾을 수 없음: " + username));

        HttpSession session = request.getSession();
        session.setAttribute("loginUser", user);

        // --- ★★★ 여기가 핵심 ★★★ ---
        // 요청 헤더의 'Accept' 값을 확인하여 API 요청인지 웹 요청인지 구분합니다.
        String acceptHeader = request.getHeader("Accept");
        boolean isApiLogin = acceptHeader != null && acceptHeader.contains("application/json");

        if (isApiLogin) {
            // 1. API 로그인일 경우 (Postman, JavaScript fetch 등) -> JSON 응답
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("message", "로그인 성공");
            responseData.put("userId", user.getUserId());
            responseData.put("nickname", user.getNickname());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String jsonResponse = objectMapper.writeValueAsString(responseData);
            response.getWriter().write(jsonResponse);
        } else {
            // 2. 웹 페이지 로그인일 경우 -> 리다이렉트
            // 부모 클래스의 기본 동작을 그대로 사용하면 로그인 후 원래 가려던 페이지나 홈페이지로 이동합니다.
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}