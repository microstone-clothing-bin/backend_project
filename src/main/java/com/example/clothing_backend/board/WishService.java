package com.example.clothing_backend.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishService {

    private final WishRepository wishRepository;

    @Transactional
    public void addWish(Long userId, Long binId) {
        // 즐찾 추가
        Wish wish = new Wish(userId, binId);
        wishRepository.save(wish);
    }

    @Transactional
    public void removeWish(Long userId, Long binId) {
        // 즐찾 제거
        wishRepository.deleteByUserIdAndBinId(userId, binId);
    }

    @Transactional(readOnly = true)
    public List<Wish> getUserWishes(Long userId) {
        // 유저가 즐찾한 Wish 리스트 조회
        return wishRepository.findByUserId(userId);
    }
}