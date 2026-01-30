package com.discussion.ryu.service;

import com.discussion.ryu.dto.discussion.*;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.DiscussionVote;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.entity.VoteType;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.UserNotAuthorException;
import com.discussion.ryu.repository.DiscussionPostRepository;
import com.discussion.ryu.repository.DiscussionVoteRepository;
import com.discussion.ryu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscussionVoteService {

    private final DiscussionPostRepository discussionPostRepository;
    private final DiscussionVoteRepository discussionVoteRepository;

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
        DiscussionVote discussionVote = DiscussionVote.builder()
                .user(user)
                .discussionPost(discussionPost)
                .voteType(voteType)
                .build();

        if (voteType == VoteType.AGREE) {
            discussionPost.incrementAgreeCount();
        } else {
            discussionPost.incrementDisagreeCount();
        }

        DiscussionVote savedVote = discussionVoteRepository.save(discussionVote);
        discussionPostRepository.save(discussionPost);

        return VoteResponse.from(savedVote, discussionPost.getId());
    }

    @Transactional
    public VoteResponse changeVote(DiscussionVote vote, VoteType newVoteType, DiscussionPost discussionPost) {
        VoteType beforeType = vote.getVoteType();

        // 이전과 같은 타입이면 그대로 반환
        if (beforeType == newVoteType) {
            return VoteResponse.from(vote, discussionPost.getId());
        }

        vote.changeVoteType(newVoteType);

        if (beforeType == VoteType.AGREE) {
            discussionPost.decrementAgreeCount();
            discussionPost.incrementDisagreeCount();
        } else {
            discussionPost.incrementAgreeCount();
            discussionPost.decrementDisagreeCount();
        }

        return VoteResponse.from(vote, discussionPost.getId());
    }

    @Transactional
    public void cancelVote(Long postId, User user) {
        DiscussionPost post = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        DiscussionVote vote = discussionVoteRepository.findByUserAndDiscussionPost(user, post)
                .orElseThrow(() -> new IllegalArgumentException("투표 기록이 없습니다."));

        if (vote.getVoteType() == VoteType.AGREE) {
            post.decrementAgreeCount();
        } else {
            post.decrementDisagreeCount();
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
