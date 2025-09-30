package com.example.clothing_backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. DB에서 아이디로 User 정보를 찾아옵니다.
        User user = userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 2. DB에서 찾아온 사용자의 Role(권한) 목록을 GrantedAuthority 형식으로 변환합니다.
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());

        // 3. Spring Security가 사용하는 표준 UserDetails 객체를 생성하여 반환합니다.
        //    이때 사용자 아이디, 비밀번호, 그리고 변환된 권한 목록을 반드시 포함해야 합니다.
        return new org.springframework.security.core.userdetails.User(
                user.getId(),
                user.getPassword(),
                authorities
        );
    }
}