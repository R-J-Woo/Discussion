package com.discussion.ryu.service;

import com.discussion.ryu.config.JwtTokenProvider;
import com.discussion.ryu.dto.discussion.*;
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

    private final UserRepository userRepository;
    private final DiscussionPostRepository discussionPostRepository;
    private final DiscussionVoteRepository discussionVoteRepository;

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

    @Transactional
    public VoteResponse vote(User user, Long postId, VoteRequestDto voteRequestDto) {
        DiscussionPost discussionPost = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        Optional<DiscussionVote> existingVote = discussionVoteRepository.findByUserAndDiscussionPost(user, discussionPost);
        if (existingVote.isPresent()) {
            return changeVote(existingVote.get(), voteRequestDto.getVoteType(), discussionPost);
        } else {
            return createVote(user, discussionPost, voteRequestDto.getVoteType());
        }
    }

    @Transactional
    public VoteResponse createVote(User user, DiscussionPost discussionPost, VoteType voteType) {
        DiscussionVote vote = new DiscussionVote();
        vote.setUser(user);
        vote.setDiscussionPost(discussionPost);
        vote.setVoteType(voteType);
        DiscussionVote savedVote = discussionVoteRepository.save(vote);

        if (voteType == VoteType.AGREE) {
            discussionPost.setAgreeCount(discussionPost.getAgreeCount() + 1);
        } else {
            discussionPost.setDisagreeCount(discussionPost.getDisagreeCount() + 1);
        }

        return VoteResponse.from(savedVote);
    }

    @Transactional
    public VoteResponse changeVote(DiscussionVote vote, VoteType newVoteType, DiscussionPost discussionPost) {
        VoteType beforeType = vote.getVoteType();

        // 이전과 같은 타입이면 그대로 반환
        if (beforeType == newVoteType) {
            return VoteResponse.from(vote);
        }

        vote.setVoteType(newVoteType);

        if (beforeType == VoteType.AGREE) {
            discussionPost.setAgreeCount(discussionPost.getAgreeCount() - 1);
            discussionPost.setDisagreeCount(discussionPost.getDisagreeCount() + 1);
        } else {
            discussionPost.setDisagreeCount(discussionPost.getDisagreeCount() - 1);
            discussionPost.setAgreeCount(discussionPost.getAgreeCount() + 1);
        }

        return VoteResponse.from(vote);
    }

    @Transactional
    public void cancelVote(Long postId, User user) {
        DiscussionPost post = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        DiscussionVote vote = discussionVoteRepository.findByUserAndDiscussionPost(user, post)
                .orElseThrow(() -> new IllegalArgumentException("투표 기록이 없습니다."));

        if (vote.getVoteType() == VoteType.AGREE) {
            post.setAgreeCount(post.getAgreeCount() - 1);
        } else {
            post.setDisagreeCount(post.getDisagreeCount() - 1);
        }

        discussionVoteRepository.delete(vote);
    }

    public VoteStatusResponse getVoteStatus(Long postId, User user) {
        DiscussionPost post = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        Optional<DiscussionVote> vote = discussionVoteRepository.findByUserAndDiscussionPost(user, post);

        VoteStatusResponse response = new VoteStatusResponse();
        response.setHasVoted(vote.isPresent());
        response.setVoteType(vote.map(DiscussionVote::getVoteType).orElse(null));
        response.setAgreeCount(post.getAgreeCount());
        response.setDisagreeCount(post.getDisagreeCount());
        return response;
    }
}
