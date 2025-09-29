package com.example.clothing_backend.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetDto {

    // 비밀번호 최소 8자리 ~ 최대 20자리 안 지키면 바로 걸림
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자리여야 합니다.")
    // 정규식: 대문자, 소문자, 숫자, 특수문자 전부 하나 이상은 무조건 포함해야 통과됨
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).+$",
            message = "비밀번호는 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String newPassword; // 새로 입력하는 비번

    private String newPasswordCheck; // 새 비번 확인용
}