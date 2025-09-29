package com.example.clothing_backend.board;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity // JPA에서 이 클래스가 DB 테이블이랑 매핑된다는 뜻
@Table(name = "board") // 실제 DB 테이블 이름 지정
@Getter
@Setter
public class Board {

    @Id // 기본키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment (DB에서 자동 증가)
    private Long boardId;

    private String title;
    private String content;
    private String nickname;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime redate;

    // 수정된 시간 저장 필드
    private LocalDateTime modifiedAt;

    private Long userId;
    private int viewCnt;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "image")
    private byte[] imageData;

    private String reviewText;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "review_image")
    private byte[] reviewImage;

    @Transient // DB에 안 넣고 서버에서만 쓰는 값 (base64 변환용)
    private String imageBase64;

    @Transient
    private String reviewImageBase64;

    @PrePersist // INSERT 되기 전에 실행되는 콜백
    public void prePersist() {
        this.redate = LocalDateTime.now(); // 등록 시간 자동 저장
    }
}