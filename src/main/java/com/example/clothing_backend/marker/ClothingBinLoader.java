package com.example.clothing_backend.marker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClothingBinLoader implements CommandLineRunner {

    private final ClothingBinRepository repository;

    public ClothingBinLoader(ClothingBinRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        // DB에 이미 데이터가 있으면 CSV 로드 생략
        if (repository.count() > 0) {
            System.out.println("이미 의류 수거함 데이터가 존재합니다. CSV 로드를 생략합니다.");
            return;
        }

        System.out.println("======================================");
        System.out.println("CSV 파싱 시작");

        // CSV 파일에서 데이터 읽기
        List<ClothingBin> binsFromCsv = loadBinsFromCsv("csv/전국_의류수거함.csv");
        saveNewBins(binsFromCsv);

        System.out.println("CSV 파싱 및 저장이 완료되었습니다.");
        System.out.println("======================================");
    }

    @Transactional
    public void saveNewBins(List<ClothingBin> binsFromCsv) {
        // 기존 좌표 가져오기 (중복 확인용)
        Set<Coordinates> existingCoordinates = repository.findAllCoordinates();

        // CSV 데이터 중 기존 좌표와 중복되지 않는 것만 필터링
        List<ClothingBin> newBins = binsFromCsv.stream()
                .filter(bin -> !existingCoordinates.contains(new Coordinates(bin.getLatitude(), bin.getLongitude())))
                .collect(Collectors.toList());

        if (!newBins.isEmpty()) {
            repository.saveAll(newBins);
            System.out.println("중복을 제외한 새로운 데이터 " + newBins.size() + "개가 저장되었습니다.");
        } else {
            System.out.println("추가할 새로운 데이터가 없습니다.");
        }
    }

    private List<ClothingBin> loadBinsFromCsv(String path) throws Exception {
        List<ClothingBin> bins = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(path).getInputStream(), "EUC-KR"))) {

            reader.readLine(); // 헤더 라인 스킵

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(",", -1);
                    if (parts.length < 4) continue;

                    // CSV의 3번째 컬럼 위도, 4번째 컬럼 경도
                    double lat = parseDoubleSafe(parts[2].trim());
                    double lon = parseDoubleSafe(parts[3].trim());

                    if (lat == 0.0 || lon == 0.0) continue;

                    bins.add(new ClothingBin(parts[0].trim(), parts[1].trim(), lat, lon));
                } catch (Exception e) {
                    // 특정 라인 오류 시 로그만 남기고 계속 진행
                }
            }
        }
        return bins;
    }

    // 안전하게 문자열을 double로 변환, 변환 실패 시 0.0 반환
    private double parseDoubleSafe(String s) {
        try {
            if (s == null || s.isEmpty()) return 0.0;
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}