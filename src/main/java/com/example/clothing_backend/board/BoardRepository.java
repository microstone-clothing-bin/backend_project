package com.example.clothing_backend.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    // JpaRepository가 기본적인 save, findById, findAll, deleteById 등을 모두 제공
}