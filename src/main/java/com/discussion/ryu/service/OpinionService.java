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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OpinionService {

    private final OpinionRepository opinionRepository;
    private final DiscussionPostRepository discussionPostRepository;
    private final NotificationManagementService notificationManagementService;

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
                .dislikeCount(0L)
                .build();

        Opinion savedOpinion = opinionRepository.save(opinion);

        // 토론글 작성자에게 알림 발송
        notificationManagementService.notifyNewOpinion(
                discussionPost.getAuthor(),
                savedOpinion,
                user.getName()
        );

        return OpinionResponse.from(savedOpinion);
    }

    public Page<OpinionResponse> getOpinionsByPost(DiscussionPost discussionPost, Pageable pageable) {
        return opinionRepository.findByDiscussionPost(discussionPost, pageable)
                .map(OpinionResponse::from);
    }

    @Transactional
    public OpinionResponse updateOpinion(Long opinionId, User user, OpinionUpdateDto opinionUpdateDto) {
        Opinion opinion = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new OpinionNotFoundException("존재하지 않는 의견입니다."));

        if (!opinion.getAuthor().getUserId().equals(user.getUserId())) {
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

        if (!opinion.getAuthor().getUserId().equals(user.getUserId())) {
            throw new UserNotAuthorException("본인이 작성한 의견만 삭제할 수 있습니다.");
        }

        opinionRepository.delete(opinion);
    }
}
