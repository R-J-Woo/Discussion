package com.discussion.ryu.service;

import com.discussion.ryu.dto.opinion.OpinionReactionRequestDto;
import com.discussion.ryu.dto.opinion.OpinionReactionResponse;
import com.discussion.ryu.entity.Opinion;
import com.discussion.ryu.entity.OpinionReaction;
import com.discussion.ryu.entity.ReactionType;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.opinion.OpinionNotFoundException;
import com.discussion.ryu.repository.OpinionReactionRepository;
import com.discussion.ryu.repository.OpinionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OpinionReactionService {

    private final OpinionRepository opinionRepository;
    private final OpinionReactionRepository opinionReactionRepository;

    @Transactional
    public OpinionReactionResponse toggleReaction(Long opinionId, User user, OpinionReactionRequestDto opinionReactionRequestDto) {
        Opinion opinion = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new OpinionNotFoundException("존재하지 않는 의견입니다."));

        Optional<OpinionReaction> existingReaction = opinionReactionRepository.findByUserAndOpinion(user, opinion);

        String message;
        ReactionType resultReaction;
        ReactionType requestType = opinionReactionRequestDto.getReactionType();

        // 기존에 reaction이 존재하면
        if (existingReaction.isPresent()) {
            OpinionReaction reaction = existingReaction.get();

            if (reaction.getReactionType() == requestType) {
                // 현재 reactionType과 동일한 버튼을 눌렀으면 취소
                handleReactionRemove(opinion, reaction);
                opinionReactionRepository.delete(reaction);
                message = requestType == ReactionType.LIKE ? "좋아요를 취소했습니다." : "싫어요를 취소했습니다.";
                resultReaction = null;
            } else {
                // 현재 reactionType과 다른 버튼을 눌렀으면 변경
                handleReactionChange(opinion, reaction.getReactionType(), requestType);
                reaction.changeReactionType(requestType);
                opinionReactionRepository.save(reaction);
                message = requestType == ReactionType.LIKE ? "좋아요로 변경했습니다." : "싫어요로 변경했습니다.";
                resultReaction = requestType;
            }
        }
        // 기존에 reaction이 존재하지 않으면 새로 생성
        else {
            OpinionReaction reaction = OpinionReaction.builder()
                    .user(user)
                    .opinion(opinion)
                    .reactionType(requestType)
                    .build();
            opinionReactionRepository.save(reaction);
            handleReactionAddition(opinion, requestType);
            message = requestType == ReactionType.LIKE ? "좋아요를 눌렀습니다." : "싫어요를 눌렀습니다.";
            resultReaction = requestType;
        }

        opinionRepository.save(opinion);
        return OpinionReactionResponse.from(opinion, resultReaction, message);
    }

    // 반응 추가 처리
    public void handleReactionAddition(Opinion opinion, ReactionType reactionType) {
        if (reactionType == ReactionType.LIKE) {
            opinion.incrementLikeCount();
        } else {
            opinion.incrementDislikeCount();
        }
    }

    // 반응 제거 처리
    public void handleReactionRemove(Opinion opinion, OpinionReaction reaction) {
        if (reaction.getReactionType() == ReactionType.LIKE) {
            opinion.decrementLikeCount();
        } else {
            opinion.decrementDislikeCount();
        }
    }

    // 반응 변경 처리
    public void handleReactionChange(Opinion opinion, ReactionType oldType, ReactionType newType) {
        // 기존 타입 카운트 감소
        if (oldType == ReactionType.LIKE) {
            opinion.decrementLikeCount();
        } else {
            opinion.decrementDislikeCount();
        }

        // 새 타입의 카운트 증가
        if (newType == ReactionType.LIKE) {
            opinion.incrementLikeCount();
        } else {
            opinion.incrementDislikeCount();
        }
    }

    // 사용자의 반응 조회
    public ReactionType getUserReactionType(Opinion opinion, User user) {
        return opinionReactionRepository.findByUserAndOpinion(user, opinion)
                .map(OpinionReaction::getReactionType)
                .orElse(null);
    }
}
