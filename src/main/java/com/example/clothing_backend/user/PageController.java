// 수정한 PageController 로그인(LoginController), 마이페이지(MyPageController), 메인페이지(HomeController) 포함 html 반환

package com.example.clothing_backend.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;

    // --- 메인 페이지 ---
    @Value("${naver.maps.clientId}")
    private String naverMapsClientId;

    // --- 기본 페이지 ---
    @GetMapping("/register.html")
    public String registerForm() { return "register"; }

    @GetMapping("/login.html")
    public String loginForm() { return "login"; }

    @GetMapping("/findIdForm")
    public String findIdForm() { return "findId"; }

    @GetMapping("/findPwForm")
    public String findPwForm() { return "findPw"; }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("naverMapsClientId", naverMapsClientId);
        return "index"; // index.html 반환
    }

    // --- <form> 제출 처리 ---
    @PostMapping("/userReg")
    public String processRegistration(User user) {
        userService.addUser(user);
        return "reg_success";
    }

    // --- [MyPageController에서 흡수 및 수정] 마이페이지 기능 ---
    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login.html";
        }
        String base64Image = userService.getProfileImageBase64(loginUser.getId());
        loginUser.setProfileImageBase64(base64Image);
        model.addAttribute("loginUser", loginUser);
        // DTO를 직접 넘기는 대신, 필요한 정보만 모델에 추가하는 것이 더 좋음
        // model.addAttribute("passwordResetDto", new PasswordResetDto());
        return "mypage";
    }

    @PostMapping("/mypage/uploadProfile")
    public String uploadProfile(@RequestParam MultipartFile profileImage, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";

        userService.saveProfileImage(profileImage, loginUser.getId());
        return "redirect:/mypage";
    }

    @PostMapping("/mypage/resetPassword")
    public String resetPassword(@RequestParam String newPassword, @RequestParam String newPasswordCheck, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";

        if (!newPassword.equals(newPasswordCheck)) {
            // 간단한 에러 처리는 RedirectAttributes를 사용하거나 프론트에서 처리
            return "redirect:/mypage?error=password_mismatch";
        }

        userService.updatePassword(loginUser.getId(), loginUser.getEmail(), newPassword);
        session.invalidate();
        return "redirect:/login.html?reset_success=true";
    }

    @PostMapping("/mypage/deleteAccount")
    public String deleteAccount(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";

        userService.deleteUser(loginUser.getId());
        session.invalidate();
        return "redirect:/";
    }
}