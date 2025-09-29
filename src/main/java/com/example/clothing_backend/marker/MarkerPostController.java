package com.example.clothing_backend.marker;

import com.example.clothing_backend.user.User;
import com.example.clothing_backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/markers")
@RequiredArgsConstructor
public class MarkerPostController {

    private final MarkerPostService markerPostService;
    private final UserService userService;

    @GetMapping("/{binId}/posts")
    public List<MarkerPostDto> getPostsForMarker(@PathVariable Long binId) {
        return markerPostService.getPostsByBinId(binId);
    }

    @PostMapping("/{binId}/posts")
    public ResponseEntity<String> createPostForMarker(
            @PathVariable Long binId,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        if (userDetails == null) {
            // SecurityConfig에서 막아주지만, 만약을 위한 최종 방어 코드
            return ResponseEntity.status(401).body("리뷰를 작성하려면 로그인이 필요합니다.");
        }

        User user = userService.getUser(userDetails.getUsername());
        markerPostService.createPost(binId, user, content, image);
        return ResponseEntity.ok("게시글이 성공적으로 등록되었습니다.");
    }
}