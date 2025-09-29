package com.example.clothing_backend.marker;

import com.example.clothing_backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarkerPostService {

    private final MarkerPostRepository markerPostRepository;
    private final ClothingBinRepository clothingBinRepository;

    // 특정 의류수거함의 모든 게시글을 DTO로 변환하여 조회
    public List<MarkerPostDto> getPostsByBinId(Long binId) {
        return markerPostRepository.findAllByClothingBin_IdOrderByCreatedAtDesc(binId)
                .stream()
                .map(post -> {
                    MarkerPostDto dto = new MarkerPostDto(post);
                    if (post.getImage() != null) {
                        dto.setImageBase64(Base64.getEncoder().encodeToString(post.getImage()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 새 게시글 생성
    @Transactional
    public void createPost(Long binId, User user, String content, MultipartFile imageFile) throws IOException {
        ClothingBin clothingBin = clothingBinRepository.findById(binId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 의류수거함 ID입니다: " + binId));

        MarkerPost post = new MarkerPost();
        post.setContent(content);
        post.setClothingBin(clothingBin);
        post.setUser(user);

        if (imageFile != null && !imageFile.isEmpty()) {
            post.setImage(imageFile.getBytes());
        }

        markerPostRepository.save(post);
    }


}