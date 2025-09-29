package com.example.clothing_backend.board;

import com.example.clothing_backend.board.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor // JdbcTemplate 자동 주입
public class BoardDao {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<Board> boardRowMapper() {
        return (rs, rowNum) -> {
            // ResultSet → Board 객체 변환
            Board board = new Board();
            board.setBoardId(rs.getLong("board_id"));
            board.setTitle(rs.getString("title"));
            board.setContent(rs.getString("content"));
            board.setNickname(rs.getString("nickname"));
            board.setRedate(rs.getTimestamp("redate").toLocalDateTime());
            board.setUserId(rs.getLong("user_id"));
            board.setViewCnt(rs.getInt("view_cnt"));
            board.setImageData(rs.getBytes("image"));
            board.setReviewText(rs.getString("review_text"));
            board.setReviewImage(rs.getBytes("review_image"));
            return board;
        };
    }

    public List<Board> getBoards(int page) {
        // 전체 게시글 조회
        String sql = "SELECT * FROM board ORDER BY board_id DESC";
        return jdbcTemplate.query(sql, boardRowMapper());
    }

    public Board getBoard(long boardId) {
        // 게시글 하나 조회
        String sql = "SELECT * FROM board WHERE board_id = ?";
        return jdbcTemplate.queryForObject(sql, boardRowMapper(), boardId);
    }

    public void incrementViewCount(long boardId) {
        // 조회수 +1
        String sql = "UPDATE board SET view_cnt = view_cnt + 1 WHERE board_id = ?";
        jdbcTemplate.update(sql, boardId);
    }

    public void addBoard(String nickname, String title, String content, Long userId, byte[] imageData, String reviewText, byte[] reviewImageData) {
        // 새 게시글 추가
        String sql = "INSERT INTO board (nickname, title, content, user_id, image, review_text, review_image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, nickname, title, content, userId, imageData, reviewText, reviewImageData);
    }

    public void updateBoard(long boardId, String title, String content) {
        // 게시글 수정 (제목, 내용만)
        String sql = "UPDATE board SET title = ?, content = ? WHERE board_id = ?";
        jdbcTemplate.update(sql, title, content, boardId);
    }

    public void deleteBoard(long boardId) {
        // 게시글 삭제
        String sql = "DELETE FROM board WHERE board_id = ?";
        jdbcTemplate.update(sql, boardId);
    }
}