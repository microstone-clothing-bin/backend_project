package com.example.clothing_backend.marker;

import lombok.RequiredArgsConstructor; // 생성자 주입을 위해 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothingBinService {

    private final ClothingBinRepository clothingBinRepository;

    public List<ClothingBin> findAll() {
        return clothingBinRepository.findAll();
    }

    // ID 목록을 기반으로 의류수거함 상세 정보 조회
    public List<ClothingBin> getBinsByIds(List<Long> binIds) {
        if (binIds == null || binIds.isEmpty()) {
            return Collections.emptyList(); // ID 목록이 비어있으면 빈 리스트를 반환
        }
        // ID 목록을 사용해 해당하는 모든 ClothingBin 데이터를 한 번에 조회
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