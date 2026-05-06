package com.discussion.ryu.service;

import com.discussion.ryu.dto.discussion.*;
import com.discussion.ryu.dto.opinion.OpinionResponse;
import com.discussion.ryu.entity.*;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.UserNotAuthorException;
import com.discussion.ryu.repository.DiscussionPostEsRepository;
import com.discussion.ryu.repository.DiscussionPostRepository;
import com.discussion.ryu.repository.DiscussionPostRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscussionPostService {

    private final DiscussionPostRepository discussionPostRepository;
    private final DiscussionPostEsRepository discussionPostEsRepository;
    private final DiscussionPostRepositoryCustom discussionPostSearchRepository;
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

        // ES 동기화
        DiscussionPostDocument doc = DiscussionPostDocument.builder()
                .id(savedPost.getId().toString())
                .postId(savedPost.getId())
                .title(savedPost.getTitle())
                .content(savedPost.getContent())
                .authorName(user.getName())
                .agreeCount(0L)
                .disagreeCount(0L)
                .createdAt(savedPost.getCreatedAt())
                .build();

        discussionPostEsRepository.save(doc);

        return DiscussionPostResponse.from(savedPost);
    }

    public Page<DiscussionPostResponse> getAllPosts(SortType sortType, Pageable pageable) {
        DiscussionSearchDto searchDto = DiscussionSearchDto.builder()
                .keyword(null)
                .searchType(null)
                .sortType(sortType)
                .build();
        return searchPosts(searchDto, pageable);
    }

    public DiscussionPostResponse getPost(Long postId, Pageable pageable) {
        DiscussionPost discussionPost = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new DiscussionPostNotFoundException("존재하지 않는 토론글입니다."));

        Page<OpinionResponse> opnions = opinionService.getOpinionsByPost(discussionPost, pageable);
        return DiscussionPostResponse.from(discussionPost, opnions);
    }

    public Page<DiscussionPostResponse> getMyPosts(User user, Pageable pageable) {
        return discussionPostRepository.findByAuthor(user, pageable)
                .map(DiscussionPostResponse::from);
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

        DiscussionPostDocument doc = DiscussionPostDocument.builder()
                .id(postId.toString())
                .postId(postId)
                .title(discussionPost.getTitle())
                .content(discussionPost.getContent())
                .authorName(discussionPost.getAuthor().getName())
                .agreeCount(discussionPost.getAgreeCount())
                .disagreeCount(discussionPost.getDisagreeCount())
                .createdAt(discussionPost.getCreatedAt())
                .updatedAt(discussionPost.getUpdatedAt())
                .build();
        discussionPostEsRepository.save(doc);

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

        // ES 소프트 삭제 — deletedAt 세팅 후 저장 (mustNot exists 필터로 검색에서 제외됨)
        discussionPostEsRepository.findById(postId.toString()).ifPresent(doc -> {
            DiscussionPostDocument deleted = DiscussionPostDocument.builder()
                    .id(doc.getId())
                    .postId(doc.getPostId())
                    .title(doc.getTitle())
                    .content(doc.getContent())
                    .authorName(doc.getAuthorName())
                    .agreeCount(doc.getAgreeCount())
                    .disagreeCount(doc.getDisagreeCount())
                    .createdAt(doc.getCreatedAt())
                    .updatedAt(doc.getUpdatedAt())
                    .deletedAt(LocalDateTime.now())
                    .build();
            discussionPostEsRepository.save(deleted);
        });
    }

    public Page<DiscussionPostResponse> searchPosts(DiscussionSearchDto searchDto, Pageable pageable) {
        String keyword = searchDto.getKeyword();
        DiscussionSearchDto.SearchType searchType = searchDto.getSearchType();
        SortType sortType = searchDto.getSortType();

        return discussionPostSearchRepository
                .searchPosts(keyword, searchType, pageable, sortType)
                .map(DiscussionPostResponse::from);
    }

}
