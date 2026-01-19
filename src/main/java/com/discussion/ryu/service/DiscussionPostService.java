package com.discussion.ryu.service;

import com.discussion.ryu.config.JwtTokenProvider;
import com.discussion.ryu.dto.discussion.DiscussionPostCreateDto;
import com.discussion.ryu.dto.discussion.DiscussionPostResponse;
import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.*;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.repository.DiscussionPostRepository;
import com.discussion.ryu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscussionPostService {

    private final UserRepository userRepository;
    private final DiscussionPostRepository discussionPostRepository;

    @Transactional
    public DiscussionPostResponse createPost(User user, DiscussionPostCreateDto discussionPostCreateDto) {

        DiscussionPost discussionPost = new DiscussionPost();
        discussionPost.setTitle(discussionPostCreateDto.getTitle());
        discussionPost.setContent(discussionPostCreateDto.getContent());
        discussionPost.setAuthor(user);

        DiscussionPost savedPost = discussionPostRepository.save(discussionPost);
        return DiscussionPostResponse.from(savedPost);
    }

    public List<DiscussionPostResponse> getAllPosts() {
        return discussionPostRepository
                .findAll()
                .stream()
                .map(DiscussionPostResponse::from)
                .collect(Collectors.toList());
    }

    public DiscussionPostResponse getPost(Long postId) {
        DiscussionPost discussionPost = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("토론글 조회에 실패하였습니다."));

        return DiscussionPostResponse.from(discussionPost);
    }
}
