package com.example.clothing_backend.marker;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clothing-bins")
@CrossOrigin(origins = "*") // 모든 도메인에서 CORS 허용
public class ClothingBinController {

    private final ClothingBinService clothingBinService;

    public ClothingBinController(ClothingBinService clothingBinService) {
        this.clothingBinService = clothingBinService;
    }

    // 전체 조회 및 반경 검색 API
    @GetMapping(produces = "application/json; charset=UTF-8")
    public List<ClothingBin> getClothingBins(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm) {
        // 모든 로직을 Service에 위임, 결과만 반환
        return clothingBinService.findClothingBins(lat, lng, radiusKm);
    }

    // 지도에서 사각형 영역 내의 bin 조회 API
    @GetMapping(value = "/in-bounds", produces = "application/json; charset=UTF-8")
    public List<ClothingBin> getBinsInBounds(
            @RequestParam double swLat,
            @RequestParam double swLng,
            @RequestParam double neLat,
            @RequestParam double neLng) {
        return clothingBinService.findBinsInBounds(swLat, swLng, neLat, neLng);
    }
}