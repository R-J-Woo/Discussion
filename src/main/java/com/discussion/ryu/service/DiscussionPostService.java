package com.discussion.ryu.service;

import com.discussion.ryu.config.JwtTokenProvider;
import com.discussion.ryu.dto.discussion.*;
import com.discussion.ryu.dto.opinion.OpinionResponse;
import com.discussion.ryu.dto.user.*;
import com.discussion.ryu.entity.*;
import com.discussion.ryu.exception.*;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.UserNotAuthorException;
import com.discussion.ryu.repository.DiscussionPostRepository;
import com.discussion.ryu.repository.DiscussionVoteRepository;
import com.discussion.ryu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscussionPostService {

    private final DiscussionPostRepository discussionPostRepository;
    private final OpinionService opinionService;

    @Transactional
    public DiscussionPostResponse createPost(User user, DiscussionPostCreateDto discussionPostCreateDto) {

        DiscussionPost discussionPost = DiscussionPost.builder()
                .title(discussionPostCreateDto.getTitle())
                .content(discussionPostCreateDto.getContent())
                .author(user)
                .agreeCount(0L)
                .disagreeCount(0L)
                .build();

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

        List<OpinionResponse> opnions = opinionService.getOpinionsByPost(discussionPost);
        return DiscussionPostResponse.from(discussionPost, opnions);
    }

    public List<DiscussionPostResponse> getMyPosts(User user) {
        return discussionPostRepository.findByAuthor(user)
                .stream()
                .map(DiscussionPostResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public DiscussionPostResponse updatePost(User user, Long postId, DiscussionPostUpdateDto discussionPostUpdateDto) {
        DiscussionPost discussionPost = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        if (!discussionPost.getAuthor().getUserId().equals(user.getUserId())) {
            throw new UserNotAuthorException("본인이 작성한 토론글만 수정할 수 있습니다.");
        }

        discussionPost.updatePost(discussionPostUpdateDto.getTitle(), discussionPostUpdateDto.getContent());
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
