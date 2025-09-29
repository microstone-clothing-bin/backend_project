// 캐싱 설정

package com.example.clothing_backend.global;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // 이 클래스가 설정 파일임을 명시 (Spring이 Bean 등록 대상으로 인식)
@EnableCaching // Spring의 캐싱 기능 활성화 (캐시 관련 어노테이션 사용 가능)
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // 이름이 "clothingBins"인 캐시 공간을 생성해서 반환
        return new ConcurrentMapCacheManager("clothingBins");
    }
}