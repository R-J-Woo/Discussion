package com.discussion.ryu.service;

import com.discussion.ryu.dto.opinion.OpinionCreateDto;
import com.discussion.ryu.dto.opinion.OpinionResponse;
import com.discussion.ryu.dto.opinion.OpinionUpdateDto;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.Opinion;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.UserNotAuthorException;
import com.discussion.ryu.exception.opinion.OpinionNotFoundException;
import com.discussion.ryu.repository.DiscussionPostRepository;
import com.discussion.ryu.repository.OpinionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OpinionService {

    private final OpinionRepository opinionRepository;
    private final DiscussionPostRepository discussionPostRepository;

    @Transactional
    public OpinionResponse createOpinion(Long postId, User user, OpinionCreateDto opinionCreateDto) {
        DiscussionPost discussionPost = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        Opinion opinion = Opinion.builder()
                .author(user)
                .discussionPost(discussionPost)
                .content(opinionCreateDto.getContent())
                .stance(opinionCreateDto.getOpinionStance())
                .likeCount(0L)
                .build();

        Opinion savedOpinion = opinionRepository.save(opinion);
        return OpinionResponse.from(savedOpinion);
    }

    public List<OpinionResponse> getOpinionsByPost(DiscussionPost discussionPost) {
        return opinionRepository.findByDiscussionPost(discussionPost)
                .stream()
                .map(OpinionResponse::from)
                .toList();
    }

    @Transactional
    public OpinionResponse updateOpinion(Long opinionId, User user, OpinionUpdateDto opinionUpdateDto) {
        Opinion opinion = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new OpinionNotFoundException("존재하지 않는 의견입니다."));

        if (!opinion.getAuthor().getUsername().equals(user.getUsername())) {
            throw new UserNotAuthorException("본인이 작성한 의견만 수정할 수 있습니다.");
        }

        opinion.updateOpinion(opinionUpdateDto.getContent(), opinionUpdateDto.getOpinionStance());
        Opinion savedOpinion = opinionRepository.save(opinion);
        return OpinionResponse.from(savedOpinion);
    }

    @Transactional
    public void deleteOpinion(Long opinionId, User user) {
        Opinion opinion = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new OpinionNotFoundException("존재하지 않는 의견입니다."));

        if (!opinion.getAuthor().getUsername().equals(user.getUsername())) {
            throw new UserNotAuthorException("본인이 작성한 의견만 삭제할 수 있습니다.");
        }

        opinionRepository.delete(opinion);
    }
}
