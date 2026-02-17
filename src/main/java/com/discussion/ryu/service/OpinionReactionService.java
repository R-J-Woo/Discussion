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
                handleReactionRemove(opinionId, reaction);
                opinionReactionRepository.delete(reaction);
                message = requestType == ReactionType.LIKE ? "좋아요를 취소했습니다." : "싫어요를 취소했습니다.";
                resultReaction = null;
            } else {
                // 현재 reactionType과 다른 버튼을 눌렀으면 변경
                handleReactionChange(opinionId, reaction.getReactionType(), requestType);
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
            handleReactionAddition(opinionId, requestType);
            message = requestType == ReactionType.LIKE ? "좋아요를 눌렀습니다." : "싫어요를 눌렀습니다.";
            resultReaction = requestType;
        }

        // 원자적 쿼리로 업데이트된 최신 opinion을 다시 조회해서 응답
        Opinion updatedOpinion = opinionRepository.findById(opinionId).orElseThrow();
        return OpinionReactionResponse.from(updatedOpinion, resultReaction, message);
    }

    // 반응 추가 처리
    public void handleReactionAddition(Long opinionId, ReactionType reactionType) {
        if (reactionType == ReactionType.LIKE) {
            opinionRepository.incrementLikeCount(opinionId);
        } else {
            opinionRepository.incrementDislikeCount(opinionId);
        }
    }

    // 반응 제거 처리
    public void handleReactionRemove(Long opinionId, OpinionReaction reaction) {
        if (reaction.getReactionType() == ReactionType.LIKE) {
            opinionRepository.decrementLikeCount(opinionId);
        } else {
            opinionRepository.decrementDislikeCount(opinionId);
        }
    }

    // 반응 변경 처리
    public void handleReactionChange(Long opinionId, ReactionType oldType, ReactionType newType) {
        // 기존 타입 카운트 감소, 새 카운트 증가
        if (oldType == ReactionType.LIKE) {
            opinionRepository.decrementLikeCount(opinionId);
            opinionRepository.incrementDislikeCount(opinionId);
        } else {
            opinionRepository.decrementDislikeCount(opinionId);
            opinionRepository.incrementLikeCount(opinionId);
        }
    }

    // 사용자의 반응 조회
    public ReactionType getUserReactionType(Opinion opinion, User user) {
        return opinionReactionRepository.findByUserAndOpinion(user, opinion)
                .map(OpinionReaction::getReactionType)
                .orElse(null);
    }
}
