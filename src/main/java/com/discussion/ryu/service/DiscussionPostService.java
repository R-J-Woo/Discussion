package com.discussion.ryu.service;

import com.discussion.ryu.config.JwtTokenProvider;
import com.discussion.ryu.dto.discussion.DiscussionPostCreateDto;
import com.discussion.ryu.dto.discussion.DiscussionPostResponse;
import com.discussion.ryu.dto.discussion.DiscussionPostUpdateDto;
import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.*;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.UserNotAuthorException;
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
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        return DiscussionPostResponse.from(discussionPost);
    }

    @Transactional
    public DiscussionPostResponse updatePost(User user, Long postId, DiscussionPostUpdateDto discussionPostUpdateDto) {
        DiscussionPost discussionPost = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        if (!discussionPost.getAuthor().getUserId().equals(user.getUserId())) {
            throw new UserNotAuthorException("본인이 작성한 토론글만 수정할 수 있습니다.");
        }

        discussionPost.setTitle(discussionPostUpdateDto.getTitle());
        discussionPost.setContent(discussionPostUpdateDto.getContent());
        discussionPostRepository.save(discussionPost);
        return DiscussionPostResponse.from(discussionPost);
    }

    @Transactional
    public void deletePost(User user, Long postId) {
        DiscussionPost discussionPost = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        if (!discussionPost.getAuthor().getUserId().equals(user.getUserId())) {
            throw new UserNotAuthorException("본인이 작성한 토론글만 삭제할 수 있습니다.");
        }

        discussionPostRepository.delete(discussionPost);
    }
}
