//// 즐겨찾기 html 반환하는 컨트롤러
//
//package com.example.clothing_backend.board;
//
//import com.example.clothing_backend.board.Wish;
//import com.example.clothing_backend.board.WishService;
//import com.example.clothing_backend.marker.ClothingBin;
//import com.example.clothing_backend.marker.ClothingBinService;
//import com.example.clothing_backend.user.User;
//import com.example.clothing_backend.user.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Controller
//@RequiredArgsConstructor
//public class WishPageController {
//
//    private final WishService wishService;
//    private final UserService userService;
//    // 의류수거함 상세 정보를 가져오기 위해 ClothingBinService를 주입합니다.
//    private final ClothingBinService clothingBinService;
//
//    @GetMapping("/wish.html")
//    public String getWishList(Model model, @AuthenticationPrincipal UserDetails userDetails) {
//
//        // UserDetails가 null이면 로그인 페이지로 리다이렉트
//        if (userDetails == null) {
//            return "redirect:/login.html";
//        }
//
//        // 현재 로그인 유저의 PK(userId) 획득
//        User user = userService.getUser(userDetails.getUsername());
//        Long userId = user.getUserId();
//
//        // User의 모든 Wish 목록 조회
//        List<Wish> wishList = wishService.getUserWishes(userId);
//
//        // binId 목록 추출
//        List<Long> binIds = wishList.stream()
//                .map(Wish::getBinId)
//                .collect(Collectors.toList());
//
//        // ClothingBinService를 사용하여 찜한 의류수거함의 상세 정보 목록 조회
//        List<ClothingBin> clothingBins = clothingBinService.getBinsByIds(binIds);
//
//        // 템플릿에 데이터 전달
//        model.addAttribute("wishes", clothingBins);
//
//        // Thymeleaf 템플릿 이름 반환
//        return "wish";
//    }
//}

package com.example.clothing_backend.board;

import com.example.clothing_backend.marker.ClothingBin;
import com.example.clothing_backend.marker.ClothingBinService;
import com.example.clothing_backend.user.User;
import com.example.clothing_backend.user.UserService;
import jakarta.servlet.http.HttpSession; // HttpSession import
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class WishPageController {

    private final WishService wishService;
    private final ClothingBinService clothingBinService;

    // [수정] @AuthenticationPrincipal 대신 HttpSession을 파라미터로 받음
    @GetMapping("/wish.html")
    public String getWishList(Model model, HttpSession session) {

        // 세션에서 "loginUser" 이름표를 가진 User 객체를 꺼냄
        User user = (User) session.getAttribute("loginUser");

        // 이름표가 없으면 비로그인 상태이므로 로그인 페이지로 리다이렉트
        if (user == null) {
            return "redirect:/login.html";
        }

        Long userId = user.getUserId();
        List<Wish> wishList = wishService.getUserWishes(userId);
        List<Long> binIds = wishList.stream()
                .map(Wish::getBinId)
                .collect(Collectors.toList());

        List<ClothingBin> clothingBins = clothingBinService.getBinsByIds(binIds);

        model.addAttribute("wishes", clothingBins);
        return "wish";
    }
}