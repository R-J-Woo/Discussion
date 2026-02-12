package com.discussion.ryu.service;

import com.discussion.ryu.config.JwtTokenProvider;
import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.user.*;
import com.discussion.ryu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(UserSignUpDto userSignUpDto) {
        if (userRepository.existsByUsername(userSignUpDto.getUsername())) {
            throw new DuplicateUsernameException("이미 존재하는 아이디입니다.");
        }

        if (userRepository.existsByName(userSignUpDto.getName())) {
            throw new DuplicateNameException("이미 존재하는 닉네임입니다.");
        }

        if (userRepository.existsByEmail(userSignUpDto.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .username(userSignUpDto.getUsername())
                .password(passwordEncoder.encode(userSignUpDto.getPassword()))
                .name(userSignUpDto.getName())
                .email(userSignUpDto.getEmail())
                .provider(AuthProvider.LOCAL)
                .providerId(userSignUpDto.getUsername())
                .build();

        userRepository.save(user);
    }

    public String login(UserLoginDto userLoginDto) {
        User user = userRepository.findByUsername(userLoginDto.getUsername())
                .orElseThrow(() -> new AuthenticationFailedException("아이디 또는 비밀번호가 틀렸습니다."));

        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            throw new AuthenticationFailedException("아이디 또는 비밀번호가 틀렸습니다.");
        }

        return jwtTokenProvider.createToken(user.getUserId(), user.getUsername());
    }

    public UserInfoResponse getMyInfo(User user) {
        return UserInfoResponse.from(user);
    }

    @Transactional
    public UserInfoResponse updateMyInfo(User user, UpdateUserInfoDto updateUserInfoDto) {

        if (userRepository.existsByNameAndUserIdNot(updateUserInfoDto.getName(), user.getUserId())) {
            throw new DuplicateNameException("이미 존재하는 닉네임입니다.");
        }

        if (userRepository.existsByEmailAndUserIdNot(updateUserInfoDto.getEmail(), user.getUserId())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다.");
        }

        user.updateInfo(updateUserInfoDto.getName(), updateUserInfoDto.getEmail());
        userRepository.save(user);

        return UserInfoResponse.from(user);
    }

    @Transactional
    public void updatePassword(User user, UpdateUserPasswordDto updateUserPasswordDto) {

        // 현재 비밀번호 일치 확인
        if (!passwordEncoder.matches(updateUserPasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 일치 확인
        if (!updateUserPasswordDto.getNewPassword().equals(updateUserPasswordDto.getNewPasswordConfirm())) {
            throw new PasswordConfirmationMismatchException("새 비밀번호가 서로 일치하지 않습니다.");
        }

        // 기존 비밀번호와 새 비밀번호가 동일한지 확인
        if (passwordEncoder.matches(updateUserPasswordDto.getNewPassword(), user.getPassword())) {
            throw new PasswordNotChangedException("기존 비밀번호와 다른 비밀번호를 사용해주세요.");
        }

        user.updatePassword(passwordEncoder.encode(updateUserPasswordDto.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteMyInfo(User user) {
        if (user.getDeletedAt() != null) {
            throw new AlreadyDeletedUserException("이미 탈퇴한 사용자입니다.");
        }

        userRepository.delete(user);
    }
}
