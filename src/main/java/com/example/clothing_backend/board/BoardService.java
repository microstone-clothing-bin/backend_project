package com.example.clothing_backend.board;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 조회만 가능, 쓰기 동작은 @Transactional 따로 붙임
public class BoardService {

    private final BoardRepository boardRepository;

    // 게시글 목록 조회 (페이징 지원)
    public Page<Board> getBoards(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }

    @Transactional
    public Board getBoard(long boardId) {
        // 게시글 조회 + 조회수 증가
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + boardId));
        board.setViewCnt(board.getViewCnt() + 1); // 조회수 1 증가
        return board;
    }

    @Transactional
    public void addBoard(String nickname, String title, String content, Long userId, byte[] imageData, String reviewText, byte[] reviewImageData) {
        // 새 게시글 추가
        Board board = new Board();
        board.setNickname(nickname);
        board.setTitle(title);
        board.setContent(content);
        board.setUserId(userId);
        board.setImageData(imageData);
        board.setReviewText(reviewText);
        board.setReviewImage(reviewImageData);
        boardRepository.save(board);
    }

    @Transactional
    public void updateBoardTextOnly(long boardId, String title, String content, Long loginUserId) {
        // 게시글 수정 (텍스트만)
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + boardId));

        if (!board.getUserId().equals(loginUserId)) {
            throw new IllegalStateException("수정 권한이 없습니다."); // 본인 아니면 예외
        }
        board.setTitle(title);
        board.setContent(content);
        board.setModifiedAt(LocalDateTime.now()); // 수정 시간 기록
    }

    @Transactional
    public void deleteBoard(long boardId) {
        // ID만으로 삭제 (권한 체크 없음 → 관리자용? → 알아봐야 할 듯)
        boardRepository.deleteById(boardId);
    }

    @Transactional
    public void deleteBoard(Long userId, long boardId) {
        // 본인 게시글만 삭제
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + boardId));
        if (!board.getUserId().equals(userId)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }
        boardRepository.delete(board);
    }
}