// MySQL 버전 8.0 이상이여야 함

package com.example.clothing_backend.marker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

// MySQL 8.0 이상 필요
public interface ClothingBinRepository extends JpaRepository<ClothingBin, Long> {

    // 데이터 로딩 시 중복 좌표 체크용
    @Query("SELECT new com.example.clothing_backend.marker.Coordinates(c.latitude, c.longitude) FROM ClothingBin c")
    Set<Coordinates> findAllCoordinates();

    // 특정 좌표가 존재하는지 확인
    boolean existsByLatitudeAndLongitude(double latitude, double longitude);

    // 반경 검색: MySQL 8.0 이상 ST_Distance_Sphere 함수 사용
    // 거리 계산 결과는 미터 단위 → radiusKm * 1000
    @Query(value = "SELECT * FROM clothing_bin c " +
            "WHERE ST_Distance_Sphere(point(c.longitude, c.latitude), point(:lng, :lat)) <= :radiusKm * 1000 " +
            "ORDER BY ST_Distance_Sphere(point(c.longitude, c.latitude), point(:lng, :lat))",
            nativeQuery = true)
    List<ClothingBin> findBinsWithinRadius(@Param("lat") double lat,
                                           @Param("lng") double lng,
                                           @Param("radiusKm") double radiusKm);

    // 지도 사각형 경계 내 데이터 조회
    @Query(value = "SELECT * FROM clothing_bin " +
            "WHERE latitude BETWEEN :swLat AND :neLat " +
            "AND longitude BETWEEN :swLng AND :neLng",
            nativeQuery = true)
    List<ClothingBin> findBinsInBounds(@Param("swLat") double swLat,
                                       @Param("swLng") double swLng,
                                       @Param("neLat") double neLat,
                                       @Param("neLng") double neLng);
}