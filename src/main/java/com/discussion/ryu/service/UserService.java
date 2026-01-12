package com.discussion.ryu.service;

import com.discussion.ryu.config.JwtTokenProvider;
import com.discussion.ryu.dto.user.UserLoginDto;
import com.discussion.ryu.dto.user.UserMeResponse;
import com.discussion.ryu.dto.user.UserSignUpDto;
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

        User user = new User(
                userSignUpDto.getUsername(),
                passwordEncoder.encode(userSignUpDto.getPassword()),
                userSignUpDto.getName(),
                userSignUpDto.getEmail(),
                "일반",
                AuthProvider.LOCAL,
                userSignUpDto.getUsername()
        );

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

    public UserMeResponse getMyInfo(User user) {
        User myUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return new UserMeResponse(
                myUser.getUsername(),
                myUser.getName(),
                myUser.getEmail(),
                myUser.getGrade(),
                myUser.getCreated_at(),
                myUser.getUpdated_at(),
                myUser.getDeleted_at()
        );
    }
}
