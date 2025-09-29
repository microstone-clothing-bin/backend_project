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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api") // 모든 API는 /api로 시작
@RequiredArgsConstructor
public class ApiController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final BoardService boardService;
    private final WishService wishService;
    private final MarkerPostService markerPostService;

    // --- 사용자 인증 (로그인, 로그아웃, 회원가입) API ---

    // 로그인
    @PostMapping("/user/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginRequest, HttpSession session) {
        String id = loginRequest.get("id");
        String password = loginRequest.get("password");
        User user = userService.getUser(id);

        Map<String, Object> response = new HashMap<>();
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 로그인 성공 시 세션에 사용자 정보 저장하고, 프론트엔드에 userId 전달
            session.setAttribute("loginUser", user);

            response.put("status", "success");
            response.put("message", "로그인 성공");
            response.put("userId", user.getUserId()); // 프론트가 저장할 PK
            response.put("nickname", user.getNickname());
            return ResponseEntity.ok(response);
        } else {
            // 로그인 실패
            response.put("status", "error");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.status(401).body(response);
        }
    }

    // 회원가입
    @PostMapping("/user/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody User user) {
        try {
            userService.addUser(user);
            return ResponseEntity.ok(Map.of("status", "success", "message", "회원가입이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "회원가입 실패: " + e.getMessage()));
        }
    }

    // 로그아웃
    @PostMapping("/user/logout")
    public ResponseEntity<Map<String, String>> logoutUser(HttpSession session) {
        session.invalidate(); // 세션 삭제
        return ResponseEntity.ok(Map.of("status", "success", "message", "로그아웃 되었습니다."));
    }

    // --- 중복 확인 API ---

    // ID 중복 확인
    @GetMapping("/user/check-id")
    public ResponseEntity<Map<String, Boolean>> checkIdDuplicate(@RequestParam("id") String id) {
        boolean isDuplicate = userService.isDuplicate("id", id);
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", isDuplicate));
    }

    // 이름 중복 확인
    @GetMapping("/user/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
        boolean isDuplicate = userService.isDuplicate("nickname", nickname);
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", isDuplicate));
    }

    // 이메일 중복 확인
    @GetMapping("/user/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestParam("email") String email) {
        boolean isDuplicate = userService.isDuplicate("email", email);
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", isDuplicate));
    }

    // --- 마이페이지 API ---

    // 사진 처리
    @PostMapping("/mypage/uploadProfile")
    public ResponseEntity<Map<String, String>> uploadProfile(@RequestParam("profileImage") MultipartFile profileImage, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        try {
            String base64Image = userService.saveProfileImage(profileImage, loginUser.getId());
            return ResponseEntity.ok(Map.of("status", "success", "profileImageUrl", base64Image));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "이미지 업로드 실패"));
        }
    }

    // 비밀번호 변경
    @PostMapping("/mypage/resetPassword")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> passwordRequest, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }

        String newPassword = passwordRequest.get("newPassword");
        // 비밀번호 확인 로직은 프론트에서 처리하는 것을 가정
        userService.updatePassword(loginUser.getId(), loginUser.getEmail(), newPassword);
        session.invalidate(); // 보안을 위해 비밀번호 변경 후 로그아웃 처리

        return ResponseEntity.ok(Map.of("status", "success", "message", "비밀번호가 변경되었습니다. 다시 로그인해주세요."));
    }

    // 계정 삭제
    @PostMapping("/mypage/deleteAccount")
    public ResponseEntity<Map<String, String>> deleteAccount(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        userService.deleteUser(loginUser.getId());
        session.invalidate();
        return ResponseEntity.ok(Map.of("status", "success", "message", "회원 탈퇴가 완료되었습니다."));
    }

    // --- 게시판 API (세션 기반) ---

    // 게시글 조회
    @GetMapping("/boards")
    public Page<Board> getBoardsApi(@PageableDefault(size = 10, sort = "boardId", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Board> boardPage = boardService.getBoards(pageable);
        // 이미지 바이트 → Base64 변환해서 프론트로 넘김
        boardPage.getContent().forEach(board -> {
            if (board.getImageData() != null)
                board.setImageBase64(Base64.getEncoder().encodeToString(board.getImageData()));
            if (board.getReviewImage() != null)
                board.setReviewImageBase64(Base64.getEncoder().encodeToString(board.getReviewImage()));
        });
        return boardPage;
    }

    // 특정 게시글 조회 
    @GetMapping("/boards/{boardId}")
    public Board getBoardApi(@PathVariable long boardId) {
        // 단일 게시글 조회 (이미지 Base64 변환 포함)
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
            @RequestParam(required = false) MultipartFile image,
            HttpSession session) throws IOException {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        byte[] imageData = (image != null && !image.isEmpty()) ? image.getBytes() : null;
        boardService.addBoard(loginUser.getNickname(), title, content, loginUser.getUserId(), imageData, null, null);
        return ResponseEntity.ok(Map.of("status", "success", "message", "게시글이 등록되었습니다."));
    }

    // --- 즐겨찾기 API (userId 사용) ---

    // 즐겨찾기 추가
    @PostMapping("/wish/add")
    public ResponseEntity<Map<String, String>> addWish(
            @RequestParam String userId,
            @RequestParam Long binId,
            HttpSession session) {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        wishService.addWish(loginUser.getUserId(), binId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "즐겨찾기가 추가되었습니다."));
    }

    // 즐겨찾기 삭제
    @DeleteMapping("/wish/remove")
    public ResponseEntity<Map<String, String>> removeWish(
            @RequestParam String userId,
            @RequestParam Long binId,
            HttpSession session) {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        wishService.removeWish(loginUser.getUserId(), binId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "즐겨찾기가 해제되었습니다."));
    }

    // 즐겨찾기 목록
    @GetMapping("/wish/list")
    public ResponseEntity<List<Long>> getUserWishes(
            @RequestParam String userId,
            HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.ok(List.of()); // 비로그인 시 빈 목록
        }
        List<Long> userWishes = wishService.getUserWishes(loginUser.getUserId()).stream()
                .map(Wish::getBinId)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userWishes);
    }

    // --- 리뷰 API ---

    // 특정 마커의 모든 리뷰 목록 조회 (로그인 불필요)
    @GetMapping("/markers/{binId}/posts")
    public List<MarkerPostDto> getPostsForMarker(@PathVariable Long binId) {
        return markerPostService.getPostsByBinId(binId);
    }

    // 특정 마커에 리뷰 등록 (세션 기반 로그인)
    @PostMapping("/markers/{binId}/posts")
    public ResponseEntity<Map<String, String>> createPostForMarker(
            @PathVariable Long binId,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session) throws IOException {

        // 세션에서 "loginUser" 이름표를 가진 User 객체를 꺼냄
        User user = (User) session.getAttribute("loginUser");

        if (user == null) {
            // 이름표가 없으면 비로그인 상태이므로 401 에러 반환
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "리뷰를 작성하려면 로그인이 필요합니다."));
        }

        markerPostService.createPost(binId, user, content, image);
        return ResponseEntity.ok(Map.of("status", "success", "message", "게시글이 성공적으로 등록되었습니다."));
    }
}