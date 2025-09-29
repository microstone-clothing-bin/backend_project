// 모든 json 받아오는 컨트롤러

package com.example.clothing_backend.user;

import com.example.clothing_backend.board.Board;
import com.example.clothing_backend.board.BoardService;
import com.example.clothing_backend.board.Wish;
import com.example.clothing_backend.board.WishService;
import com.example.clothing_backend.marker.MarkerPostDto;
import com.example.clothing_backend.marker.MarkerPostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api") // 모든 API는 /api로 시작
@RequiredArgsConstructor
public class ApiController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final BoardService boardService;
    private final WishService wishService;
    private final MarkerPostService markerPostService;

    // --- 사용자 인증 및 마이페이지 API ---

    @PostMapping("/user/login")
    public ResponseEntity<Map<String, Object>> loginUser(
            @RequestBody Map<String,String> loginRequest, HttpSession session) {
        String id = loginRequest.get("id");
        String password = loginRequest.get("password");
        User user = userService.getUser(id);

        Map<String, Object> response = new HashMap<>();
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // VVV 수정 VVV: 세션 키 이름을 "loginUser"로 통일합니다.
            session.setAttribute("loginUser", user);
            response.put("status", "success");
            response.put("message", "로그인 성공");
            response.put("userId", user.getUserId());
            response.put("nickname", user.getNickname());
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/user/register")
    public ResponseEntity<Map<String, String>> registerUser(
            @RequestBody User user) {
        try {
            userService.addUser(user);
            return ResponseEntity.ok(Map.of("status", "success", "message", "회원가입이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "회원가입 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/user/logout")
    public ResponseEntity<Map<String, String>> logoutUser(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("status", "success", "message", "로그아웃 되었습니다."));
    }

    @GetMapping("/user/check-id")
    public ResponseEntity<Map<String, Boolean>> checkIdDuplicate
            (@RequestParam("id") String id) {
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", userService.isDuplicate("id", id)));
    }

    @GetMapping("/user/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplicate(
            @RequestParam("nickname") String nickname) {
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", userService.isDuplicate("nickname", nickname)));
    }

    @GetMapping("/user/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(
            @RequestParam("email") String email) {
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", userService.isDuplicate("email", email)));
    }

    @PostMapping("/mypage/uploadProfile")
    public ResponseEntity<Map<String, String>> uploadProfile(
            @RequestParam("profileImage") MultipartFile profileImage, HttpSession session) {
        // VVV 수정 VVV: 세션 키 이름을 "loginUser"로 통일합니다.
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        try {
            // VVV 수정 VVV: 서비스에 ID 전달 시 String 타입으로 변환합니다. (PageController와 동일한 문제 해결)
            String base64Image = userService.saveProfileImage(profileImage, String.valueOf(loginUser.getUserId()));
            return ResponseEntity.ok(Map.of("status", "success", "profileImageUrl", base64Image));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "이미지 업로드 실패"));
        }
    }

    @PostMapping("/mypage/resetPassword")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody Map<String, String> passwordRequest, HttpSession session) {
        // VVV 수정 VVV: 세션 키 이름을 "loginUser"로 통일합니다.
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        String newPassword = passwordRequest.get("newPassword");
        // VVV 수정 VVV: 서비스에 DB 고유 번호(userId)와 로그인ID(id)를 정확히 전달합니다.
        userService.updatePassword(String.valueOf(loginUser.getUserId()), loginUser.getId(), newPassword);
        session.invalidate();
        return ResponseEntity.ok(Map.of("status", "success", "message", "비밀번호가 변경되었습니다. 다시 로그인해주세요."));
    }

    @PostMapping("/mypage/deleteAccount")
    public ResponseEntity<Map<String, String>> deleteAccount(HttpSession session) {
        // VVV 수정 VVV: 세션 키 이름을 "loginUser"로 통일합니다.
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        // VVV 수정 VVV: 서비스에 DB 고유 번호인 userId를 전달합니다.
        userService.deleteUser(String.valueOf(loginUser.getUserId()));
        session.invalidate();
        return ResponseEntity.ok(Map.of("status", "success", "message", "회원 탈퇴가 완료되었습니다."));
    }

    // --- 게시판 API ---

    @GetMapping("/boards")
    public Page<Board> getBoardsApi(
            @PageableDefault(size = 10, sort = "boardId", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Board> boardPage = boardService.getBoards(pageable);
        boardPage.getContent().forEach(board -> {
            if (board.getImageData() != null)
                board.setImageBase64(Base64.getEncoder().encodeToString(board.getImageData()));
            if (board.getReviewImage() != null)
                board.setReviewImageBase64(Base64.getEncoder().encodeToString(board.getReviewImage()));
        });
        return boardPage;
    }

    @GetMapping("/boards/{boardId}")
    public Board getBoardApi(
            @PathVariable long boardId) {
        Board board = boardService.getBoard(boardId);
        if (board.getImageData() != null)
            board.setImageBase64(Base64.getEncoder().encodeToString(board.getImageData()));
        if (board.getReviewImage() != null)
            board.setReviewImageBase64(Base64.getEncoder().encodeToString(board.getReviewImage()));
        return board;
    }

    @PostMapping("/boards")
    public ResponseEntity<Map<String, String>> writeBoard(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image, HttpSession session) throws IOException {
        // VVV 수정 VVV: 세션 키 이름을 "loginUser"로 통일합니다.
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        byte[] imageData = (image != null && !image.isEmpty()) ? image.getBytes() : null;
        boardService.addBoard(loginUser.getNickname(), title, content, loginUser.getUserId(), imageData, null, null);
        return ResponseEntity.ok(Map.of("status", "success", "message", "게시글이 등록되었습니다."));
    }

    // --- 즐겨찾기 API (세션 기반으로 통일) ---

    @PostMapping("/wish/add/{binId}")
    public ResponseEntity<Map<String, String>> addWish(@PathVariable Long binId, HttpSession session) {
        // VVV 수정 VVV: 세션 키 이름을 "loginUser"로 통일합니다.
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        wishService.addWish(user.getUserId(), binId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "즐겨찾기가 추가되었습니다."));
    }

    @DeleteMapping("/wish/remove/{binId}")
    public ResponseEntity<Map<String, String>> removeWish(@PathVariable Long binId, HttpSession session) {
        // VVV 수정 VVV: 세션 키 이름을 "loginUser"로 통일합니다.
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        wishService.removeWish(user.getUserId(), binId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "즐겨찾기가 해제되었습니다."));
    }

    @GetMapping("/wish/list")
    public ResponseEntity<List<Long>> getUserWishes(HttpSession session) {
        // VVV 수정 VVV: 세션 키 이름을 "loginUser"로 통일합니다.
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Long> userWishes = wishService.getUserWishes(user.getUserId())
                .stream()
                .map(Wish::getBinId)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userWishes);
    }

    // --- 리뷰 API (세션 기반) ---

    @GetMapping("/markers/{binId}/posts")
    public List<MarkerPostDto> getPostsForMarker(@PathVariable Long binId) {
        return markerPostService.getPostsByBinId(binId);
    }

    @PostMapping("/markers/{binId}/posts")
    public ResponseEntity<Map<String, String>> createPostForMarker(@PathVariable Long binId, @RequestParam String content, @RequestParam(required = false) MultipartFile image, HttpSession session) throws IOException {
        // VVV 수정 VVV: 세션 키 이름을 "loginUser"로 통일합니다.
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "리뷰를 작성하려면 로그인이 필요합니다."));
        }
        markerPostService.createPost(binId, user, content, image);
        return ResponseEntity.ok(Map.of("status", "success", "message", "게시글이 성공적으로 등록되었습니다."));
    }
}