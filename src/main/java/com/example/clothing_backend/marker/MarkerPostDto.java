package com.example.clothing_backend.marker;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MarkerPostDto {
    private Long postId;
    private String content;
    private String imageBase64;
    private LocalDateTime createdAt;
    private String authorNickname; // 작성자 닉네임만 포함

    // Entity -> DTO 변환을 위한 생성자
    public MarkerPostDto(MarkerPost entity) {
        this.postId = entity.getPostId();
        this.content = entity.getContent();
        this.createdAt = entity.getCreatedAt();
        this.authorNickname = entity.getUser().getNickname(); // User 객체에서 닉네임만 가져옴
    }
}