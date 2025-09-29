package com.example.clothing_backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public void addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    // 로그인 시 사용자 조회
    public User getUser(String id) {
        return userRepository.findById(id).orElse(null);
    }

    // 중복 확인
    public boolean isDuplicate(String type, String value) {
        if ("id".equals(type)) {
            return userRepository.existsById(value);
        } else if ("nickname".equals(type)) {
            return userRepository.existsByNickname(value);
        } else if ("email".equals(type)) {
            return userRepository.existsByEmail(value);
        }
        return false;
    }

    // 역할 조회
    public List<String> getRoles(Long userId) {
        return userRepository.findRolesByUserId(userId);
    }

    // 아이디 찾기
    public String findIdByNicknameAndEmail(String nickname, String email) {
        return userRepository.findByNicknameAndEmail(nickname, email)
                .map(User::getId)
                .orElse(null);
    }

    // 비밀번호 찾기
    public String findPwByIdAndEmail(String id, String email) {
        return userRepository.findByIdAndEmail(id, email)
                .map(User::getPassword)
                .orElse(null);
    }

    // 프로필 이미지 저장
    @Transactional
    public String saveProfileImage(MultipartFile file, String id) {
        User user = getUser(id);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        try {
            byte[] bytes = file.getBytes();
            user.setProfileImageBlob(bytes);
            userRepository.save(user); // 변경사항 저장
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("프로필 이미지 저장 실패", e);
        }
    }

    // 프로필 이미지 조회
    public String getProfileImageBase64(String id) {
        User user = getUser(id);
        if (user != null && user.getProfileImageBlob() != null) {
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(user.getProfileImageBlob());
        }
        return null;
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(String id) {
        User user = getUser(id);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    // 비밀번호 재설정
    @Transactional
    public void updatePassword(String id, String email, String newPassword) {
        User user = userRepository.findByIdAndEmail(id, email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

