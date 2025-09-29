package com.example.clothing_backend.marker;

import lombok.RequiredArgsConstructor; // 생성자 주입을 위해 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 성능 최적화
@RequiredArgsConstructor // final 필드를 사용하는 생성자를 자동으로 생성
public class ClothingBinService {

    private final ClothingBinRepository clothingBinRepository;

    // ID 목록을 기반으로 의류수거함 상세 정보 조회
    public List<ClothingBin> getBinsByIds(List<Long> binIds) {
        // JpaRepository의 findAllById 메서드를 사용하여 ID 목록에 해당하는 모든 데이터를 조회
        return clothingBinRepository.findAllById(binIds);
    }

    // 반경 기반 검색 또는 전체 조회
    public List<ClothingBin> findClothingBins(Double lat, Double lng, Double radiusKm) {
        if (lat != null && lng != null && radiusKm != null) {
            // 위치 기반 검색
            return clothingBinRepository.findBinsWithinRadius(lat, lng, radiusKm);
        } else {
            // 전체 조회
            return clothingBinRepository.findAll();
        }
    }

    // 지도 사각형 경계 내 데이터 조회
    public List<ClothingBin> findBinsInBounds(double swLat, double swLng, double neLat, double neLng) {
        return clothingBinRepository.findBinsInBounds(swLat, swLng, neLat, neLng);
    }

}