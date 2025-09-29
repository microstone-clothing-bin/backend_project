// 즐겨찾기 json 반환하는 컨트롤러 → 이젠 apicontrolelr가 전부 담당
//
//package com.example.clothing_backend.board;
//
//import com.example.clothing_backend.user.User;
//import com.example.clothing_backend.user.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/wish")
//@RequiredArgsConstructor
//public class WishApiController {
//
//    private final WishService wishService;
//    private final UserService userService; // User 정보를 얻기 위해 추가
//
//    // 즐찾 추가 (로그인 체크 적용)
//    // POST /api/wish/add/{binId}
//    @PostMapping("/add/{binId}")
//    public ResponseEntity<String> addWish(
//            @PathVariable Long binId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        if (userDetails == null) {
//            // 비로그인 시 401 반환 (프론트에서 로그인 페이지로 리다이렉트 유도)
//            return ResponseEntity.status(401).body("로그인이 필요합니다.");
//        }
//
//        // UserDetails에서 로그인 ID (username)를 가져와 User 객체 획득
//        User user = userService.getUser(userDetails.getUsername());
//
//        // WishService는 User의 PK인 userId를 사용
//        wishService.addWish(user.getUserId(), binId);
//        return ResponseEntity.ok("즐겨찾기가 추가되었습니다.");
//    }
//
//    // 즐찾 제거 (로그인 체크 적용)
//    // DELETE /api/wish/remove/{binId}
//    @DeleteMapping("/remove/{binId}")
//    public ResponseEntity<String> removeWish(
//            @PathVariable Long binId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        if (userDetails == null) {
//            return ResponseEntity.status(401).body("로그인이 필요합니다.");
//        }
//
//        User user = userService.getUser(userDetails.getUsername());
//        wishService.removeWish(user.getUserId(), binId);
//        return ResponseEntity.ok("즐겨찾기가 해제되었습니다.");
//    }
//
//    // 유저가 즐겨찾기 한 binId 리스트 반환 (로그인 체크 적용)
//    // GET /api/wish/list
//    @GetMapping("/list")
//    public List<Long> getUserWishes(@AuthenticationPrincipal UserDetails userDetails) {
//        if (userDetails == null) {
//            // 비로그인 사용자에게는 빈 목록을 반환하여 JS에서 처리
//            return List.of();
//        }
//
//        User user = userService.getUser(userDetails.getUsername());
//        // 유저가 즐찾한 binId 리스트 반환
//        return wishService.getUserWishes(user.getUserId())
//                .stream()
//                .map(Wish::getBinId)
//                .collect(Collectors.toList());
//    }
//}