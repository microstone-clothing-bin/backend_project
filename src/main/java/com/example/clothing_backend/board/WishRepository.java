package com.example.clothing_backend.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {

    // 특정 유저가 즐찾한 모든 Wish 조회
    List<Wish> findByUserId(Long userId);

    // 특정 유저의 특정 bin 즐찾 삭제
    void deleteByUserIdAndBinId(Long userId, Long binId);
}