package com.example.clothing_backend.marker;

// 위도와 경도를 담기 위한 record (Java 14 이상)
// record 사용 → 생성자, getter, equals, hashCode, toString 자동 생성
public record Coordinates(double latitude, double longitude) {
}