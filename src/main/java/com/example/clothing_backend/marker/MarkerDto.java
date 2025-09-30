// 마커 DTO

package com.example.clothing_backend.marker;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MarkerDto {
    private double lat;
    private double lng;
    private String name;
}