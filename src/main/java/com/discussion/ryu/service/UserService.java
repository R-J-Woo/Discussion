package com.discussion.ryu.service;

import com.discussion.ryu.config.JwtTokenProvider;
import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void signup(UserSignUpDto userSignUpDto) {
        if (userRepository.existsByUsername(userSignUpDto.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        if (userRepository.existsByName(userSignUpDto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        User user = new User();
        user.setUsername(userSignUpDto.getUsername());
        user.setPassword(passwordEncoder.encode(userSignUpDto.getPassword()));
        user.setName(userSignUpDto.getName());
        user.setEmail(userSignUpDto.getEmail());
        user.setGrade("일반");
        user.setProvider(AuthProvider.LOCAL);
        user.setProviderId(userSignUpDto.getUsername());
        userRepository.save(user);
    }

    public String login(UserLoginDto userLoginDto) {
        User user = userRepository.findByUsername(userLoginDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 틀렸습니다."));

        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 틀렸습니다.");
        }

        return jwtTokenProvider.createToken(user.getUserId(), user.getUsername());
    }

    public UserInfoResponse getMyInfo(User user) {
        User myUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return UserInfoResponse.from(myUser);
    }

    public UserInfoResponse updateMyInfo(User user, UpdateUserInfoDto updateUserInfoDto) {
        User myUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (userRepository.existsByName(updateUserInfoDto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        myUser.setName(updateUserInfoDto.getName());
        myUser.setEmail(updateUserInfoDto.getEmail());
        userRepository.save(myUser);

        return UserInfoResponse.from(myUser);
    }

    public UserInfoResponse updatePassword(User user, UpdateUserPasswordDto updateUserPasswordDto) {
        User myUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 일치 확인
        if (!passwordEncoder.matches(updateUserPasswordDto.getCurrentPassword(), myUser.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 일치 확인
        if (!updateUserPasswordDto.getNewPassword().equals(updateUserPasswordDto.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("새 비밀번호가 서로 일치하지 않습니다.");
        }

        // 기존 비밀번호와 새 비밀번호가 동일한지 확인
        if (passwordEncoder.matches(updateUserPasswordDto.getNewPassword(), myUser.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호와 다른 비밀번호를 사용해주세요.");
        }

        myUser.setPassword(passwordEncoder.encode(updateUserPasswordDto.getNewPassword()));
        userRepository.save(myUser);

        return UserInfoResponse.from(myUser);
    }
}
