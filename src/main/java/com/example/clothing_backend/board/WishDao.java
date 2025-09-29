package com.example.clothing_backend.board;

import com.example.clothing_backend.board.Wish;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WishDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public WishDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 즐찾 추가
    public int addWish(Wish wish) {
        // userId + binId가 이미 존재하면 created_at만 갱신
        String sql = "INSERT INTO wish (user_id, bin_id) VALUES (:userId, :binId) ON DUPLICATE KEY UPDATE created_at = CURRENT_TIMESTAMP";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("userId", wish.getUserId())
                .addValue("binId", wish.getBinId());
        return jdbcTemplate.update(sql, param);
    }

    // 회원 즐찾 목록 조회
    public List<Wish> getUserWishes(String userId) {
        String sql = "SELECT * FROM wish WHERE user_id = :userId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("userId", userId),
                new BeanPropertyRowMapper<>(Wish.class));
    }

    // 즐찾 삭제
    public int removeWish(String userId, Long binId) {
        String sql = "DELETE FROM wish WHERE user_id = :userId AND bin_id = :binId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("binId", binId);
        return jdbcTemplate.update(sql, param);
    }
}