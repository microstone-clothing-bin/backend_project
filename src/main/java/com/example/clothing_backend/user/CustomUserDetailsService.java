package com.example.clothing_backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 → 성능 최적화
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 로그인할 때 "아이디"로 DB에서 유저 찾기
        // 없으면 UsernameNotFoundException 터뜨려서 로그인 실패 처리
        return userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }
}
