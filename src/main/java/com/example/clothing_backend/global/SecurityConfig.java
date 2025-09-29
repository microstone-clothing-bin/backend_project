//// 보안 설정 (건들지 말기)
//
//package com.example.clothing_backend.global;
//
//import com.example.clothing_backend.global.CustomAuthenticationSuccessHandler;
//import com.example.clothing_backend.user.CustomUserDetailsService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import java.util.Arrays;
//
//@Configuration
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final CustomUserDetailsService customUserDetailsService;
//    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                // 전역 CORS 설정 적용
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//
//                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
//
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers(
//                                "/", "/css/**", "/js/**", "/api/**",
//                                "/login", "/login.html", "/register.html", "/userReg",
//                                "/findIdForm", "/findId", "/findPwForm", "/findPw",
//                                "/share", "/board"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/login.html")
//                        .loginProcessingUrl("/login")
//                        .usernameParameter("id")
//                        .passwordParameter("password")
//                        .successHandler(customAuthenticationSuccessHandler)
//                        .failureUrl("/login.html?error=true")
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/")
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                )
//                .userDetailsService(customUserDetailsService);
//
//        return http.build();
//    }
//
//    // 전역 CORS 설정 Bean 생성
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // 프론트엔드 서버 주소(http://localhost:5173)를 허용
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
//        // 모든 종류의 HTTP 메소드(GET, POST 등)를 허용
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        // 모든 종류의 HTTP 헤더를 허용
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        // 쿠키 등 자격 증명 정보를 함께 보내는 것을 허용
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        // 모든 URL 경로("/**")에 대해 위에서 만든 CORS 설정을 적용
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}

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
    // [삭제] CustomAuthenticationSuccessHandler는 더 이상 웹/API 분리에 방해만 됨

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // API는 CSRF 비활성화

                .authorizeHttpRequests(authz -> authz
                        // [핵심] /api/** 경로는 컨트롤러에서 직접 인증을 처리하므로 일단 모두 허용!
                        .requestMatchers("/api/**").permitAll()
                        // 그 외 웹 페이지 경로 중 공개할 경로들
                        .requestMatchers(
                                "/", "/css/**", "/js/**",
                                "/login.html", "/login", // 웹 로그인 경로는 formLogin이 처리
                                "/register.html", "/userReg",
                                "/findIdForm", "/findId", "/findPwForm", "/findPw",
                                "/share", "/board"
                        ).permitAll()
                        // 위에 명시된 경로 외의 모든 요청은 인증을 요구
                        .anyRequest().authenticated()
                )
                // [수정] 웹 페이지 전용 로그인 설정 (단순화)
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .usernameParameter("id")
                        .defaultSuccessUrl("/", true) // 성공 시 무조건 메인으로
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}