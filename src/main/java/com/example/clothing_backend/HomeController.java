package com.example.clothing_backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // application.properties에 있는 네이버 지도 클라이언트 ID 값을 주입받음
    @Value("${naver.maps.clientId}")
    private String naverMapsClientId;

    @GetMapping("/")
    public String home(Model model) {
        // 모델에 네이버 지도 클라이언트 ID를 담아서 뷰로 전달
        model.addAttribute("naverMapsClientId", naverMapsClientId);
        return "index"; // templates/index.html 파일을 찾아서 렌더링
    }
}