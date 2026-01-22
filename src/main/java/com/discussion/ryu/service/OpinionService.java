package com.discussion.ryu.service;

import com.discussion.ryu.dto.opinion.OpinionCreateDto;
import com.discussion.ryu.dto.opinion.OpinionResponse;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.Opinion;
import com.discussion.ryu.entity.User;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
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
}
