// 즐겨찾기 html 반환하는 컨트롤러

package com.example.clothing_backend.board;

import com.example.clothing_backend.marker.ClothingBin;
import com.example.clothing_backend.marker.ClothingBinService;
import com.example.clothing_backend.user.User;
import com.example.clothing_backend.user.UserService;
import jakarta.servlet.http.HttpSession;
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