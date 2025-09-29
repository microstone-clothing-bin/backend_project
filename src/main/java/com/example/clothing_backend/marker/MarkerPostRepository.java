package com.example.clothing_backend.marker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarkerPostRepository extends JpaRepository<MarkerPost, Long> {

    // 의류수거함(bin) ID를 기준으로 모든 게시글을 최신순으로 찾아오는 메소드
    List<MarkerPost> findAllByClothingBin_IdOrderByCreatedAtDesc(Long binId);
}

