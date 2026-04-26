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
        // 비관적 락으로 토론글 조회 - 이 구문이 WHERE 절을 완료할 때까지 행 잠금
        DiscussionPost discussionPost = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        // 기존 투표 확인
        Optional<DiscussionVote> existingVote = discussionVoteRepository.findByUserAndDiscussionPost(user, discussionPost);

        if (existingVote.isPresent()) {
            return changeVote(discussionPost, existingVote.get(), voteRequestDto.getVoteType());
        } else {
            return createVote(discussionPost, user, voteRequestDto.getVoteType());
        }
    }

    private VoteResponse createVote(DiscussionPost discussionPost, User user, VoteType voteType) {
        DiscussionVote discussionVote = DiscussionVote.builder()
                .user(user)
                .discussionPost(discussionPost)
                .voteType(voteType)
                .build();

        // 투표 레코드 저장
        DiscussionVote savedVote = discussionVoteRepository.save(discussionVote);

        // 엔티티의 메서드를 통해 카운트 증가
        if (voteType == VoteType.AGREE) {
            discussionPostRepository.incrementAgreeCount(discussionPost.getId());
        } else {
            discussionPostRepository.incrementDisagreeCount(discussionPost.getId());
        }

        return VoteResponse.from(savedVote, discussionPost.getId());
    }

    private VoteResponse changeVote(DiscussionPost discussionPost, DiscussionVote vote, VoteType newVoteType) {
        VoteType beforeType = vote.getVoteType();

        // 이전과 같은 타입이면 변경하지 않음
        if (beforeType == newVoteType) {
            return VoteResponse.from(vote, discussionPost.getId());
        }

        // 투표 타입 변경
        vote.changeVoteType(newVoteType);

        // 엔티티 메서드를 통해 카운트 업데이트
        if (beforeType == VoteType.AGREE) {
            discussionPostRepository.decrementAgreeCount(discussionPost.getId());
            discussionPostRepository.incrementDisagreeCount(discussionPost.getId());
        } else {
            discussionPostRepository.incrementAgreeCount(discussionPost.getId());
            discussionPostRepository.decrementDisagreeCount(discussionPost.getId());
        }

        return VoteResponse.from(vote, discussionPost.getId());
    }

    /**
     * 투표 취소
     * 비관적 락으로 토론글을 조회하여 동시성 제어
     */
    @Transactional
    public void cancelVote(Long postId, User user) {
        // 비관적 락으로 토론글 조회
        DiscussionPost post = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        DiscussionVote vote = discussionVoteRepository.findByUserAndDiscussionPost(user, post)
                .orElseThrow(() -> new VoteNotFoundException("투표 기록이 없습니다."));

        // 투표 삭제
        discussionVoteRepository.delete(vote);

        // 엔티티 카운트 감소
        if (vote.getVoteType() == VoteType.AGREE) {
            discussionPostRepository.decrementAgreeCount(post.getId());
        } else {
            discussionPostRepository.decrementDisagreeCount(post.getId());
        }
    }

    /**
     * 투표 상태 조회 (읽기 전용)
     * 락이 필요 없음 - 읽기 작업만 수행
     */
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
