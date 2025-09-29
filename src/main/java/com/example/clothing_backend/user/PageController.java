// 수정한 PageController 로그인(LoginController), 마이페이지(MyPageController), 메인페이지(HomeController), 게시판(BoardController) 포함 html 반환

package com.example.clothing_backend.user;

import com.example.clothing_backend.board.Board;
import com.example.clothing_backend.board.BoardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;
    private final BoardService boardService;

    // --- 메인 페이지 ---
    @Value("${naver.maps.clientId}")
    private String naverMapsClientId;

    // --- 기본 페이지 ---
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("naverMapsClientId", naverMapsClientId);
        return "index";
    }

    @GetMapping("/register.html")
    public String registerForm() { return "register"; }

    @GetMapping("/login.html")
    public String loginForm() { return "login"; }

    @GetMapping("/findIdForm")
    public String findIdForm() { return "findId"; }

    @GetMapping("/findPwForm")
    public String findPwForm() { return "findPw"; }

    @PostMapping("/userReg")
    public String processRegistration(User user) {
        userService.addUser(user);
        return "reg_success";
    }

    // --- 마이페이지 ---
    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";

        String base64Image = userService.getProfileImageBase64(loginUser.getId());
        loginUser.setProfileImageBase64(base64Image);
        model.addAttribute("loginUser", loginUser);
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

    // --- 게시판 페이지 ---

    @GetMapping("/share")
    public String boardList(@PageableDefault(page = 0, size = 10, sort = "boardId", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        Page<Board> paging = boardService.getBoards(pageable);
        model.addAttribute("paging", paging);
        return "list";
    }

    @GetMapping("/board")
    public String boardDetail(@RequestParam("boardId") long boardId, Model model) {
        Board board = boardService.getBoard(boardId);
        if (board.getImageData() != null) {
            board.setImageBase64(Base64.getEncoder().encodeToString(board.getImageData()));
        }
        model.addAttribute("board", board);
        return "detail";
    }

    @GetMapping("/writeform")
    public String writeForm(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("naverMapsClientId", naverMapsClientId);

        return "writeform";
    }

    @PostMapping("/write")
    public String write(@RequestParam String title, @RequestParam String content, @RequestParam(required = false) MultipartFile image, HttpSession session) throws IOException {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";
        byte[] imageData = (image != null && !image.isEmpty()) ? image.getBytes() : null;
        boardService.addBoard(loginUser.getNickname(), title, content, loginUser.getUserId(), imageData, null, null);
        return "redirect:/share";
    }

    @GetMapping("/updateform")
    public String updateForm(@RequestParam("boardId") long boardId, HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";
        Board board = boardService.getBoard(boardId);
        if (!board.getUserId().equals(loginUser.getUserId())) return "redirect:/share";
        model.addAttribute("board", board);
        model.addAttribute("loginUser", loginUser);
        return "updateform";
    }

    @PostMapping("/update")
    public String updateBoard(@RequestParam("boardId") long boardId, @RequestParam("title") String title, @RequestParam("content") String content, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";
        boardService.updateBoardTextOnly(boardId, title, content, loginUser.getUserId());
        return "redirect:/board?boardId=" + boardId;
    }

    @GetMapping("/delete")
    public String deleteBoard(@RequestParam("boardId") long boardId, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";
        Board board = boardService.getBoard(boardId);
        if (loginUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                || board.getUserId().equals(loginUser.getUserId())) {
            boardService.deleteBoard(boardId);
        }
        return "redirect:/share";
    }
}