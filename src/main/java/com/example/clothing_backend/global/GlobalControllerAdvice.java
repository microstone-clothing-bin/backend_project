package com.example.clothing_backend.global;

import com.example.clothing_backend.user.User;
import com.example.clothing_backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    @ModelAttribute("loginUser")
    public User loginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 사용자가 인증되지 않았거나, Principal이 UserDetails 타입이 아니면 null 반환
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return null;
        }

        // Principal에서 사용자 ID(username)를 가져옴
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        // DB에서 전체 User 정보를 조회하여 반환
        return userRepository.findById(username).orElse(null);
    }
}