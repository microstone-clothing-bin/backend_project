// BoardController는 html만을 반환함

package com.example.clothing_backend.board;

import com.example.clothing_backend.user.LoginInfo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

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
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) return "redirect:/login.html";
        model.addAttribute("loginInfo", loginInfo);
        return "writeform";
    }

    @PostMapping("/write")
    public String write(@RequestParam String title, @RequestParam String content, @RequestParam(required = false) MultipartFile image, HttpSession session) throws IOException {
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) return "redirect:/login.html";
        byte[] imageData = (image != null && !image.isEmpty()) ? image.getBytes() : null;
        boardService.addBoard(loginInfo.getNickname(), title, content, loginInfo.getUserId(), imageData, null, null);
        return "redirect:/share";
    }

    @GetMapping("/updateform")
    public String updateForm(@RequestParam("boardId") long boardId, HttpSession session, Model model) {
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) return "redirect:/login.html";
        Board board = boardService.getBoard(boardId);
        if (!board.getUserId().equals(loginInfo.getUserId())) return "redirect:/share";
        model.addAttribute("board", board);
        model.addAttribute("loginInfo", loginInfo);
        return "updateform";
    }

    @PostMapping("/update")
    public String updateBoard(@RequestParam("boardId") long boardId, @RequestParam("title") String title, @RequestParam("content") String content, HttpSession session) {
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) return "redirect:/login.html";
        boardService.updateBoardTextOnly(boardId, title, content, loginInfo.getUserId());
        return "redirect:/board?boardId=" + boardId;
    }

    @GetMapping("/delete")
    public String deleteBoard(@RequestParam("boardId") long boardId, HttpSession session) {
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) return "redirect:/login.html";
        Board board = boardService.getBoard(boardId);
        if (loginInfo.getRoles().contains("ROLE_ADMIN") || board.getUserId().equals(loginInfo.getUserId())) {
            boardService.deleteBoard(boardId);
        }
        return "redirect:/share";
    }

    // [삭제] /api/boards 와 /api/boards/{boardId} 메소드는 ApiController로 이동했으므로 삭제!
}