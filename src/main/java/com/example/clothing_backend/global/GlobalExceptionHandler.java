package com.example.clothing_backend.global;

import com.example.clothing_backend.user.UserNotFoundException; // import 추가
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * "사용자를 찾을 수 없는" 명확한 경우만 처리합니다. (오래된 세션 등)
     * 이 예외가 발생하면 세션을 무효화하고 로그인 페이지로 보냅니다.
     */
    @ExceptionHandler({UserNotFoundException.class, DataIntegrityViolationException.class})
    public Object handleStaleSessionException(Exception ex, HttpServletRequest request, HttpSession session) {
        log.error("Stale session data detected! Invalidating session.", ex);
        session.invalidate();

        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "세션이 만료되었거나 유효하지 않습니다. 다시 로그인해주세요."));
        } else {
            return "redirect:/login.html?error=session_expired";
        }
    }

    /**
     * 위에서 잡지 못한 그 외 모든 예측하지 못한 RuntimeException을 처리합니다.
     * 이 핸들러는 로그만 남기고, 사용자를 강제로 로그아웃시키지 않습니다.
     * 이를 통해 우리는 숨어있는 진짜 원인을 파악할 수 있습니다.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleGenericRuntimeException(RuntimeException ex) {
        // VVV 매우 중요: 어떤 에러인지 정확히 알기 위해 전체 스택 트레이스를 출력합니다.
        log.error("An unexpected runtime exception occurred!", ex);

        // 사용자에게는 간단한 에러 메시지만 보여줍니다.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error
                .body(Map.of("status", "error", "message", "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."));
    }
}