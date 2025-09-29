//package com.example.clothing_backend.user;
//
//
//import com.example.clothing_backend.user.LoginInfo;
//import com.example.clothing_backend.user.User;
//import com.example.clothing_backend.user.UserService;
//import jakarta.servlet.http.HttpSession;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api")
//@RequiredArgsConstructor
//public class ApiController {
//
//    private final UserService userservice;
//    private final PasswordEncoder passwordEncoder;
//
//    // User API
//
//    @PostMapping("/user/register")
//    public Map<String, Object> registerUser(@RequestParam String id,
//                                            @RequestParam String password,
//                                            @RequestParam String passwordCheck,
//                                            @RequestParam String nickname,
//                                            @RequestParam String email) {
//        Map<String, Object> response = new HashMap<>();
//        if (!password.equals(passwordCheck)) {
//            response.put("status", "error");
//            response.put("message", "비밀번호 불일치");
//            return response;
//        }
//
//        User user = new User();
//        user.setId(id);
//        user.setPassword(password);
//        user.setNickname(nickname);
//        user.setEmail(email);
//
//        userservice.addUser(user); // User 객체를 서비스에 넘김
//
//        response.put("status", "success");
//        response.put("message", "회원가입 성공");
//        return response;
//    }
//
//    @PostMapping("/user/login")
//    public Map<String, Object> loginUser(@RequestParam String id,
//                                         @RequestParam String password,
//                                         HttpSession session) {
//        Map<String, Object> response = new HashMap<>();
//
//        User user = userservice.getUser(id);
//        if (user == null) {
//            response.put("status", "error");
//            response.put("message", "사용자를 찾을 수 없습니다.");
//            return response;
//        }
//
//        // BCrypt 비밀번호 확인
//        if (!passwordEncoder.matches(password, user.getPassword())) {
//            response.put("status", "error");
//            response.put("message", "비밀번호가 일치하지 않습니다.");
//            return response;
//        }
//
//        // 프로필 이미지 Base64 세팅
//        String base64Image = userservice.getProfileImageBase64(user.getId());
//        if (base64Image != null) user.setProfileImageBase64(base64Image);
//
//        session.setAttribute("loginUser", user);
//
//        // 로그인 정보 세션에 저장 (권한 포함)
//        LoginInfo loginInfo = new LoginInfo(user.getUserId(), user.getId(), user.getNickname());
//        loginInfo.setRoles(userservice.getRoles(user.getUserId()));
//        session.setAttribute("loginInfo", loginInfo);
//
//        response.put("status", "success");
//        response.put("message", "로그인 성공");
//        response.put("userId", user.getId());
//        response.put("nickname", user.getNickname());
//        return response;
//    }
//
//    @PostMapping("/user/logout")
//    public Map<String, Object> logoutUser(HttpSession session) {
//        session.invalidate(); // 세션 삭제
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "success");
//        response.put("message", "로그아웃 완료");
//        return response;
//    }
//
//    // MyPage API
//
//    @CrossOrigin(
//            origins = {"http://localhost:5173"},      allowCredentials = "true"
//    ) // 도메인 허용 쿠키 허용 쿠키허용 추가
//
//    @PostMapping("/mypage/uploadProfile")
//    public Map<String, Object> uploadProfile(@RequestParam MultipartFile profileImage, HttpSession session) throws IOException {
//        Map<String, Object> response = new HashMap<>();
//        User loginUser = (User) session.getAttribute("loginUser");
//        if (loginUser == null) {
//            response.put("status", "error");
//            response.put("message", "로그인 필요");
//            return response;
//        }
//
//        // 이미지 bytes를 Base64로 변환 후 User에 세팅
//        String base64 = userservice.saveProfileImage(profileImage, loginUser.getId());
//        loginUser.setProfileImageBase64(base64);
//        session.setAttribute("loginUser", loginUser);
//
//        response.put("status", "success");
//        response.put("message", "프로필 업로드 성공");
//        return response;
//    }
//
//    @PostMapping("/mypage/resetPassword")
//    public Map<String, Object> resetPassword(@RequestParam String newPassword,
//                                             @RequestParam String newPasswordCheck,
//                                             HttpSession session) {
//        Map<String, Object> response = new HashMap<>();
//        User loginUser = (User) session.getAttribute("loginUser");
//        if (loginUser == null) {
//            response.put("status", "error");
//            response.put("message", "로그인 필요");
//            return response;
//        }
//        if (!newPassword.equals(newPasswordCheck)) {
//            response.put("status", "error");
//            response.put("message", "비밀번호 불일치");
//            return response;
//        }
//
//        userservice.updatePassword(loginUser.getId(), loginUser.getEmail(), newPassword);
//        session.invalidate(); // 비밀번호 변경 후 세션 초기화
//
//        response.put("status", "success");
//        response.put("message", "비밀번호 변경 성공");
//        return response;
//    }
//
//    @PostMapping("/mypage/deleteAccount")
//    public Map<String, Object> deleteAccount(HttpSession session) {
//        Map<String, Object> response = new HashMap<>();
//        User loginUser = (User) session.getAttribute("loginUser");
//        if (loginUser == null) {
//            response.put("status", "error");
//            response.put("message", "로그인 필요");
//            return response;
//        }
//
//        userservice.deleteUser(loginUser.getId());
//        session.invalidate(); // 탈퇴 후 세션 초기화
//
//        response.put("status", "success");
//        response.put("message", "회원 탈퇴 성공");
//        return response;
//    }
//}

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
            @RequestParam Long binId) {

        User user = userService.getUser(userId);
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "사용자를 찾을 수 없습니다."));
        }

        wishService.addWish(user.getUserId(), binId);  // 내부적으로 PK 사용
        return ResponseEntity.ok(Map.of("status", "success", "message", "즐겨찾기가 추가되었습니다."));
    }

    // 즐겨찾기 삭제
    @DeleteMapping("/wish/remove")
    public ResponseEntity<Map<String, String>> removeWish(
            @RequestParam String userId,
            @RequestParam Long binId) {

        User user = userService.getUser(userId);
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "사용자를 찾을 수 없습니다."));
        }

        wishService.removeWish(user.getUserId(), binId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "즐겨찾기가 해제되었습니다."));
    }

    // 즐겨찾기 목록
    @GetMapping("/wish/list")
    public ResponseEntity<List<Long>> getUserWishes(@RequestParam String userId) {
        User user = userService.getUser(userId);
        if (user == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<Long> userWishes = wishService.getUserWishes(user.getUserId()).stream()
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