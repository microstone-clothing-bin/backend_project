// HTML 페이지 반환용 컨트롤러 (json은 UserController)

package com.example.clothing_backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;

    // --- 웹 페이지를 보여주는 모든 GET 요청 ---
    @GetMapping("/register.html")
    public String registerForm() { return "register"; }

    @GetMapping("/login.html")
    public String loginForm() { return "login"; }

    @GetMapping("/findIdForm")
    public String findIdForm() { return "findId"; }

    @GetMapping("/findPwForm")
    public String findPwForm() { return "findPw"; }

    // --- 웹 페이지 <form> 제출을 처리하는 POST 요청 ---
    @PostMapping("/userReg")
    public String processRegistration(User user) {
        userService.addUser(user);
        return "reg_success";
    }

    @PostMapping("/findId")
    public String findId(@RequestParam String nickname, @RequestParam String email, Model model) {
        String foundId = userService.findIdByNicknameAndEmail(nickname, email);
        model.addAttribute("message", foundId != null
                ? "회원님의 아이디는 [ " + foundId + " ] 입니다."
                : "일치하는 회원이 없습니다.");
        return "findId";
    }

    @PostMapping("/findPw")
    public String findPw(@RequestParam String id, @RequestParam String email, Model model) {
        // 실제 비밀번호 찾기 로직(메일 전송 등)은 UserService에 구현 필요
        userService.findPwByIdAndEmail(id, email);
        model.addAttribute("message", "가입하신 이메일로 임시 비밀번호 관련 안내를 전송했습니다.");
        return "findPw";
    }
}
