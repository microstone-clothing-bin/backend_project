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

    // userId를 사용해 데이터베이스에서 사용자를 찾아 반환
    public User getUserById(Long userId) {
        // VVV 수정 VVV: orElse(null) 대신 예외를 발생시켜 일관성을 유지합니다.
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("ID가 " + userId + "인 사용자를 찾을 수 없습니다."));
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
            // VVV 수정 VVV: RuntimeException -> UserNotFoundException 으로 변경
            throw new UserNotFoundException("ID가 " + id + "인 사용자를 찾을 수 없습니다.");
        }
        try {
            byte[] bytes = file.getBytes();
            user.setProfileImageBlob(bytes);
            userRepository.save(user); // 변경사항 저장
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            // 이미지 처리 실패는 일반 런타임 예외로 처리
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
        } else {
            // VVV 수정 VVV: RuntimeException -> UserNotFoundException 으로 변경
            throw new UserNotFoundException("ID가 " + id + "인 사용자를 찾을 수 없습니다.");
        }
    }

    // 비밀번호 재설정
    @Transactional
    public void updatePassword(String id, String email, String newPassword) {
        // VVV 수정 VVV: RuntimeException -> UserNotFoundException 으로 변경
        User user = userRepository.findByIdAndEmail(id, email)
                .orElseThrow(() -> new UserNotFoundException("ID 또는 이메일이 일치하는 사용자를 찾을 수 없습니다."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}