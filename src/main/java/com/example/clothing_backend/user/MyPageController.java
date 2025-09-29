package com.example.clothing_backend.user;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final UserService userService;

    // 마이페이지
    @GetMapping
    public String myPage(@AuthenticationPrincipal User loginUser, Model model) {
        if (loginUser == null) {
            return "redirect:/login.html";
        }
        // 프로필 이미지 base64로 뽑아서 넣어줌
        String base64Image = userService.getProfileImageBase64(loginUser.getId());
        loginUser.setProfileImageBase64(base64Image);

        // 로그인한 유저랑 비번 리셋 폼 모델로 보냄
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("passwordResetDto", new PasswordResetDto()); // 비번 변경용 DTO 미리 심어줌
        return "mypage";
    }

    // 프로필 이미지 업로드 처리
    @PostMapping("/uploadProfile")
    public String uploadProfile(@RequestParam MultipartFile profileImage, @AuthenticationPrincipal User loginUser) throws IOException {
        userService.saveProfileImage(profileImage, loginUser.getId()); // 서비스에서 저장함
        return "redirect:/mypage"; // 끝나면 다시 마이페이지로
    }

    // 비밀번호 재설정
    @PostMapping("/resetPassword")
    public String resetPassword(@Valid @ModelAttribute("passwordResetDto") PasswordResetDto passwordDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal User loginUser,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        // 새 비번이랑 확인 비번 안 맞으면 에러
        if (!passwordDto.getNewPassword().equals(passwordDto.getNewPasswordCheck())) {
            bindingResult.rejectValue("newPasswordCheck", "passwordInCorrect", "새 비밀번호가 일치하지 않습니다.");
        }

        // 에러 있으면 → 메시지 담아서 다시 마이페이지로 튕김
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mypage";
        }

        // 비번 업데이트
        userService.updatePassword(loginUser.getId(), loginUser.getEmail(), passwordDto.getNewPassword());

        // 세션 끊고 로그인 페이지로 이동
        session.invalidate();
        return "redirect:/login.html?reset_success=true";
    }

    // 회원 탈퇴 처리
    @PostMapping("/deleteAccount")
    public String deleteAccount(@AuthenticationPrincipal User loginUser, HttpSession session) {
        if (loginUser == null) {
            return "redirect:/login.html"; // 로그인 안 된 놈이면 튕김
        }
        // 서비스 타서 DB에서 회원 삭제
        userService.deleteUser(loginUser.getId());
        session.invalidate(); // 세션도 끊고
        return "redirect:/"; // 홈으로 돌려보냄
    }
}