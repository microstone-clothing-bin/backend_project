//'오래된 세션'으로 인해 사용자를 찾지 못하는 상황을 위한 전용 예외 클래스

package com.example.clothing_backend.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 이 예외가 발생하면 HTTP 404 Not Found 상태 코드를 응답하도록 설정할 수 있습니다.
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "User Not Found")
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}