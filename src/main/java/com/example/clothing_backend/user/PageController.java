//// HTML 페이지 반환용 컨트롤러
//
//package com.example.clothing_backend.user;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@Controller
//@RequiredArgsConstructor
//public class PageController {
//
//    private final UserService userService;
//
//    // --- 웹 페이지를 보여주는 모든 GET 요청 ---
//    @GetMapping("/register.html")
//    public String registerForm() { return "register"; }
//
//    @GetMapping("/login.html")
//    public String loginForm() { return "login"; }
//
//    @GetMapping("/findIdForm")
//    public String findIdForm() { return "findId"; }
//
//    @GetMapping("/findPwForm")
//    public String findPwForm() { return "findPw"; }
//
//    // --- 웹 페이지 <form> 제출을 처리하는 POST 요청 ---
//    @PostMapping("/userReg")
//    public String processRegistration(User user) {
//        userService.addUser(user);
//        return "reg_success";
//    }
//
//    @PostMapping("/findId")
//    public String findId(@RequestParam String nickname, @RequestParam String email, Model model) {
//        String foundId = userService.findIdByNicknameAndEmail(nickname, email);
//        model.addAttribute("message", foundId != null
//                ? "회원님의 아이디는 [ " + foundId + " ] 입니다."
//                : "일치하는 회원이 없습니다.");
//        return "findId";
//    }
//
//    @PostMapping("/findPw")
//    public String findPw(@RequestParam String id, @RequestParam String email, Model model) {
//        // 실제 비밀번호 찾기 로직(메일 전송 등)은 UserService에 구현 필요
//        userService.findPwByIdAndEmail(id, email);
//        model.addAttribute("message", "가입하신 이메일로 임시 비밀번호 관련 안내를 전송했습니다.");
//        return "findPw";
//    }
//}

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