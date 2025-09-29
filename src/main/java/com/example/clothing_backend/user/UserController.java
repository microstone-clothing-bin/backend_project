// json 반환용 컨트롤러 (html은 pageController)

package com.example.clothing_backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/user") // API 경로 통일
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 앱/웹 프론트엔드를 위한 API 전용 엔드포인트

    @GetMapping("/check-id")
    public ResponseEntity<Map<String, Boolean>> checkIdDuplicate(@RequestParam("id") String id) {
        boolean isDuplicate = userService.isDuplicate("id", id);
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", isDuplicate));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
        boolean isDuplicate = userService.isDuplicate("nickname", nickname);
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", isDuplicate));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestParam("email") String email) {
        boolean isDuplicate = userService.isDuplicate("email", email);
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", isDuplicate));
    }
}