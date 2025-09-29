package com.example.clothing_backend.marker;

import com.example.clothing_backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "marker_post")
@Getter
@Setter
public class MarkerPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private String content;

    // DB 종류에 상관없이 작동하도록 @JdbcTypeCode 사용
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] image;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // --- 관계 설정 ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bin_id", nullable = false)
    private ClothingBin clothingBin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}