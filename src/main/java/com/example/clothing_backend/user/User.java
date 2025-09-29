package com.example.clothing_backend.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "`user`") // DB 테이블 이름이 예약어(user)라서 역따옴표(`) 처리
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long userId; // 유저 PK

    @Column(unique = true, nullable = false)
    private String id; // 로그인 ID

    @Column(nullable = false)
    private String password; // 비밀번호 (BCrypt 해시)

    @Column(unique = true, nullable = false)
    private String nickname; // 닉네임

    @Column(nullable = false)
    private String email; // 이메일

    private LocalDateTime redate; // 가입일(자동 세팅)

    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] profileImageBlob; // 프로필 이미지(바이너리 저장)

    @Transient
    private String profileImageBase64; // 프로필 이미지 → 화면에서 Base64 인코딩용 (DB X)

    // user_role 테이블로 연결 (유저가 여러 역할 가질 수 있음)
    @ManyToMany(fetch = FetchType.EAGER) // 권한은 항상 즉시 로딩
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // DB에 INSERT 되기 전에 실행 → 가입일 자동 세팅
    @PrePersist
    public void prePersist() {
        this.redate = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Role 엔티티에 있는 roleName("ROLE_USER" 등)을
        // Spring Security에서 쓰는 SimpleGrantedAuthority로 변환
        return this.roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() { return this.id; } // 로그인 ID 기준으로 인증
    @Override
    public boolean isAccountNonExpired() { return true; } // 계정 만료 X
    @Override
    public boolean isAccountNonLocked() { return true; } // 잠김 X
    @Override
    public boolean isCredentialsNonExpired() { return true; } // 비번 만료 X
    @Override
    public boolean isEnabled() { return true; } // 계정 활성화 O
}
