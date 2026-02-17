package com.discussion.ryu.service;

import com.discussion.ryu.dto.discussion.*;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.DiscussionVote;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.entity.VoteType;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.VoteNotFoundException;
import com.discussion.ryu.repository.DiscussionPostRepository;
import com.discussion.ryu.repository.DiscussionVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

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
            return changeVote(existingVote.get(), voteRequestDto.getVoteType(), postId);
        } else {
            return createVote(user, discussionPost, voteRequestDto.getVoteType());
        }
    }

    private VoteResponse createVote(User user, DiscussionPost discussionPost, VoteType voteType) {
        DiscussionVote discussionVote = DiscussionVote.builder()
                .user(user)
                .discussionPost(discussionPost)
                .voteType(voteType)
                .build();

        if (voteType == VoteType.AGREE) {
            discussionPostRepository.incrementAgreeCount(discussionPost.getId());
        } else {
            discussionPostRepository.incrementDisagreeCount(discussionPost.getId());
        }

        DiscussionVote savedVote = discussionVoteRepository.save(discussionVote);

        return VoteResponse.from(savedVote, discussionPost.getId());
    }

    private VoteResponse changeVote(DiscussionVote vote, VoteType newVoteType, Long postId) {
        VoteType beforeType = vote.getVoteType();

        // 이전과 같은 타입이면 그대로 반환
        if (beforeType == newVoteType) {
            return VoteResponse.from(vote, postId);
        }

        vote.changeVoteType(newVoteType);

        if (beforeType == VoteType.AGREE) {
            discussionPostRepository.decrementAgreeCount(postId);
            discussionPostRepository.incrementDisagreeCount(postId);
        } else {
            discussionPostRepository.incrementAgreeCount(postId);
            discussionPostRepository.decrementDisagreeCount(postId);
        }

        return VoteResponse.from(vote, postId);
    }

    @Transactional
    public void cancelVote(Long postId, User user) {
        DiscussionPost post = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        DiscussionVote vote = discussionVoteRepository.findByUserAndDiscussionPost(user, post)
                .orElseThrow(() -> new VoteNotFoundException("투표 기록이 없습니다."));

        if (vote.getVoteType() == VoteType.AGREE) {
            discussionPostRepository.decrementAgreeCount(postId);
        } else {
            discussionPostRepository.decrementDisagreeCount(postId);
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
