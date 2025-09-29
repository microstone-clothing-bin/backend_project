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

    @Value("${naver.maps.clientId}")
    private String naverMapsClientId;

    // --- 기본 페이지 ---
    // (기존 코드와 동일)
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("naverMapsClientId", naverMapsClientId);
        return "index";
    }
    // ... (register, login, findId, findPw 등 폼 반환 메소드들은 그대로)
    @GetMapping("/register.html")
    public String registerForm() { return "register"; }

    @GetMapping("/login.html")
    public String loginForm() { return "login"; }

    @GetMapping("/findIdForm")
    public String findIdForm() { return "findId"; }

    @PostMapping("/findId")
    public String findId(@RequestParam("nickname") String nickname,
                         @RequestParam("email") String email,
                         Model model) {

        String foundId = userService.findIdByNicknameAndEmail(nickname, email);

        String message; // 뷰에 전달할 메시지 변수

        if (foundId != null) {
            // 아이디를 찾았을 경우
            message = "회원님의 아이디는 [ " + foundId + " ] 입니다.";
        } else {
            // 아이디를 찾지 못했을 경우
            message = "입력하신 정보와 일치하는 아이디가 없습니다.";
        }

        // 모델에 메시지를 담아서
        model.addAttribute("message", message);

        // ★★★ 핵심 ★★★
        // findId.html 뷰를 다시 렌더링하여 message를 전달합니다.
        return "findId";
    }

    @GetMapping("/findPwForm")
    public String findPwForm() { return "findPw"; }

    @PostMapping("/findPw")
    public String findPw(@RequestParam("id") String id,
                         @RequestParam("email") String email,
                         HttpSession session,
                         Model model) {

        // 1. 아이디와 이메일로 사용자가 존재하는지 확인합니다.
        User user = userService.getUser(id); // ID로 우선 사용자를 찾습니다.

        // 2. 사용자가 존재하고, 이메일이 일치하는지 확인합니다.
        if (user != null && user.getEmail().equals(email)) {
            // 3. 일치한다면, 비밀번호를 변경할 사용자의 ID를 임시 세션에 저장합니다.
            //    (로그인 세션인 "loginUser"와는 다른 이름으로 저장해야 혼동이 없습니다.)
            session.setAttribute("idForPwReset", id);

            // 4. 비밀번호 재설정 페이지로 리디렉션합니다.
            return "redirect:/resetPwForm";
        } else {
            // 5. 일치하는 사용자가 없다면, 에러 메시지를 모델에 담아 다시 findPw 페이지를 보여줍니다.
            model.addAttribute("message", "입력하신 정보와 일치하는 사용자가 없습니다.");
            return "findPw";
        }
    }

    // 1. 비밀번호 재설정 폼(resetPw.html)을 보여주는 GET 메소드
    @GetMapping("/resetPwForm")
    public String resetPwForm(HttpSession session) {
        // 1단계에서 저장한 세션 정보가 없으면, 비정상적인 접근으로 보고 로그인 페이지로 보냅니다.
        if (session.getAttribute("idForPwReset") == null) {
            return "redirect:/login.html";
        }
        return "resetPw";
    }


    // 2. 새 비밀번호로 업데이트하는 POST 메소드
    @PostMapping("/resetPw")
    public String resetPw(@RequestParam("id") String id,
                          @RequestParam("newPassword") String newPassword,
                          @RequestParam("newPasswordCheck") String newPasswordCheck,
                          HttpSession session) {

        // 비정상적인 접근 방지
        String idForReset = (String) session.getAttribute("idForPwReset");
        if (idForReset == null || !idForReset.equals(id)) {
            return "redirect:/login.html";
        }

        // 새 비밀번호와 확인용 비밀번호가 일치하지 않으면, 다시 재설정 페이지로 보냅니다.
        if (!newPassword.equals(newPasswordCheck)) {
            return "redirect:/resetPwForm?error=true";
        }

        // DB에서 사용자 정보를 가져와서 비밀번호를 업데이트합니다.
        User user = userService.getUser(id);
        userService.updatePassword(user.getId(), user.getEmail(), newPassword);

        // 사용했던 임시 세션은 보안을 위해 반드시 제거합니다.
        session.removeAttribute("idForPwReset");

        // 비밀번호 변경이 완료되었으므로, 성공 메시지와 함께 로그인 페이지로 보냅니다.
        return "redirect:/login.html?reset_success=true";
    }

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

        // VVV 수정 VVV: getProfileImageBase64는 String 타입의 로그인 id를 받습니다.
        String base64Image = userService.getProfileImageBase64(loginUser.getId());
        loginUser.setProfileImageBase64(base64Image);
        model.addAttribute("loginUser", loginUser);
        return "mypage";
    }

    @PostMapping("/mypage/uploadProfile")
    public String uploadProfile(@RequestParam MultipartFile profileImage, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";

        // VVV 수정 VVV: saveProfileImage는 String 타입의 로그인 id를 받습니다.
        userService.saveProfileImage(profileImage, loginUser.getId());
        return "redirect:/mypage";
    }

    @PostMapping("/mypage/resetPassword")
    public String resetPassword(@RequestParam String newPassword, @RequestParam String newPasswordCheck, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";

        if (!newPassword.equals(newPasswordCheck)) {
            return "redirect:/mypage?error=password_mismatch";
        }

        // updatePassword는 String 타입 id, email을 받으므로 그대로 사용합니다.
        userService.updatePassword(loginUser.getId(), loginUser.getEmail(), newPassword);
        session.invalidate();
        return "redirect:/login.html?reset_success=true";
    }

    @PostMapping("/mypage/deleteAccount")
    public String deleteAccount(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";

        // VVV 수정 VVV: deleteUser는 String 타입의 로그인 id를 받습니다.
        userService.deleteUser(loginUser.getId());
        session.invalidate();
        return "redirect:/";
    }

    // --- 게시판 페이지 --- (대부분 Long 타입 userId를 사용하므로 변경점 거의 없음)
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

        model.addAttribute("loginInfo", loginUser);
        model.addAttribute("naverMapsClientId", naverMapsClientId);
        return "writeform";
    }

    @PostMapping("/write")
    public String write(@RequestParam String title, @RequestParam String content, @RequestParam(required = false) MultipartFile image, HttpSession session) throws IOException {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";

        byte[] imageData = (image != null && !image.isEmpty()) ? image.getBytes() : null;
        // addBoard는 Long 타입 userId를 사용하므로 그대로 둡니다.
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
        model.addAttribute("loginInfo", loginUser);
        return "updateform";
    }

    @PostMapping("/update")
    public String updateBoard(@RequestParam("boardId") long boardId, @RequestParam("title") String title, @RequestParam("content") String content, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login.html";

        // updateBoardTextOnly는 Long 타입 userId를 사용하므로 그대로 둡니다.
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