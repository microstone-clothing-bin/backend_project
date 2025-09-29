package com.example.clothing_backend.board;

import com.example.clothing_backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "wish")
@Getter
@Setter
@NoArgsConstructor
public class Wish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 기본키, auto_increment
    private Long id;

    @Column(nullable = false)
    private Long userId; // 찜한 유저 ID

    @Column(nullable = false)
    private Long binId; // 찜한 옷 수거함 ID

    @Column(updatable = false)
    private LocalDateTime createdAt; // 생성 시각, 수정 불가

    @PrePersist
    protected void onCreate() {
        // 엔티티 저장 직전에 자동으로 현재 시간 설정
        this.createdAt = LocalDateTime.now();
    }

    public Wish(Long userId, Long binId) {
        this.userId = userId;
        this.binId = binId;
    }
}