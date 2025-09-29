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
@RequiredArgsConstructor // final 필드(BoardService) 자동 생성자 주입
public class BoardController {

    private final BoardService boardService;

    // HTML 페이지 보여주는 메소드들 각각 뭔지는 아래 주석 참고

    @GetMapping("/share")
    public String boardList(@PageableDefault(page = 0, size = 10, sort = "boardId", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        // 게시글 리스트 페이징 처리
        Page<Board> paging = boardService.getBoards(pageable);
        model.addAttribute("paging", paging);
        return "list"; // list.html 반환
    }

    // 게시판 상세 페이지
    @GetMapping("/board")
    public String boardDetail(@RequestParam("boardId") long boardId, Model model) {
        Board board = boardService.getBoard(boardId);

        // DB의 이미지(byte[])를 HTML에 표시할 Base64 문자열로 변환
        if (board.getImageData() != null) {
            board.setImageBase64(Base64.getEncoder().encodeToString(board.getImageData()));
        }

        model.addAttribute("board", board);
        return "detail"; // templates/detail.html
    }

    @GetMapping("/writeform")
    public String writeForm(HttpSession session, Model model) {
        // 로그인 체크 → 안했으면 로그인 페이지로 튕김
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) return "redirect:/login.html";
        model.addAttribute("loginInfo", loginInfo);
        return "writeform";
    }

    @PostMapping("/write")
    public String write(@RequestParam String title, @RequestParam String content, @RequestParam(required = false) MultipartFile image, HttpSession session) throws IOException {
        // 게시글 작성 → 로그인 필수 + 이미지 파일 있으면 바이트로 변환
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) return "redirect:/login.html";
        byte[] imageData = (image != null && !image.isEmpty()) ? image.getBytes() : null;
        boardService.addBoard(loginInfo.getNickname(), title, content, loginInfo.getUserId(), imageData, null, null);
        return "redirect:/share";
    }

    @GetMapping("/updateform")
    public String updateForm(@RequestParam("boardId") long boardId, HttpSession session, Model model) {
        // 본인 게시글 수정인지 확인 (본인 or redirect)
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
        // 게시글 수정 (제목/내용만 가능)
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) return "redirect:/login.html";
        boardService.updateBoardTextOnly(boardId, title, content, loginInfo.getUserId());
        return "redirect:/board?boardId=" + boardId;
    }

    @GetMapping("/delete")
    public String deleteBoard(@RequestParam("boardId") long boardId, HttpSession session) {
        // 삭제: 관리자 or 본인만 가능
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) return "redirect:/login.html";
        Board board = boardService.getBoard(boardId);
        if (loginInfo.getRoles().contains("ROLE_ADMIN") || board.getUserId().equals(loginInfo.getUserId())) {
            boardService.deleteBoard(boardId);
        }
        return "redirect:/share";
    }

    // API (JSON 반환)
    // @ResponseBody → 데이터를 JSON으로 내려줌

    @GetMapping("/api/boards")
    @ResponseBody
    public Page<Board> getBoardsApi(@PageableDefault(size = 10, sort = "boardId", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Board> boardPage = boardService.getBoards(pageable);
        // 이미지 바이트 → Base64 변환해서 프론트로 넘김
        boardPage.getContent().forEach(board -> {
            if (board.getImageData() != null)
                board.setImageBase64(Base64.getEncoder().encodeToString(board.getImageData()));
            if (board.getReviewImage() != null)
                board.setReviewImageBase64(Base64.getEncoder().encodeToString(board.getReviewImage()));
        });
        return boardPage;
    }

    @GetMapping("/api/boards/{boardId}")
    @ResponseBody
    public Board getBoardApi(@PathVariable long boardId) {
        // 단일 게시글 조회 (이미지 Base64 변환 포함)
        Board board = boardService.getBoard(boardId);
        if (board.getImageData() != null)
            board.setImageBase64(Base64.getEncoder().encodeToString(board.getImageData()));
        if (board.getReviewImage() != null)
            board.setReviewImageBase64(Base64.getEncoder().encodeToString(board.getReviewImage()));
        return board;
    }
}