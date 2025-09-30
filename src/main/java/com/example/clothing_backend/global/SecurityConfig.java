package com.example.clothing_backend.global;

import com.example.clothing_backend.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // CSRF 보호 예외 처리 (API 개발 시)

                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                // 정적 자원
                                "/css/**", "/js/**", "/images/**",

                                // 인증 없이 접근 가능한 페이지
                                "/", "/login", "/login.html", "/register.html", "/userReg",
                                "/findIdForm", "/findId", "/findPwForm", "/findPw", "/resetPwForm", "/resetPw",
                                "/share", "/board",

                                // 인증 없이 호출 가능한 API
                                "/api/user/register", // <-- 이 줄을 추가해주세요!
                                "/api/user/check-id", "/api/user/check-nickname", "/api/user/check-email",
                                "/api/markers",
                                "/api/wish/list",
                                "/api/clothing-bins", // <-- '.'이 오타인 것 같아 수정했습니다.
                                "/api/clothing-bins/in-bounds",
                                "/api/markers/*/posts"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/api/user/login")
                        .usernameParameter("id")
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/login.html?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}