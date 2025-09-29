package com.example.clothing_backend.marker;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clothing_bin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자는 protected로 제한
public class ClothingBin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 auto_increment
    private Long id;

    @Column(name = "road_address")
    private String roadAddress; // 도로명 주소

    @Column(name = "land_lot_address")
    private String landLotAddress; // 지번 주소

    @Column(name = "latitude")
    private double latitude; // 위도

    @Column(name = "longitude")
    private double longitude; // 경도

    // 생성자 (주소 + 좌표)
    public ClothingBin(String roadAddress, String landLotAddress, double latitude, double longitude) {
        this.roadAddress = roadAddress;
        this.landLotAddress = landLotAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}