package com.discussion.ryu.service;

import com.discussion.ryu.dto.discussion.*;
import com.discussion.ryu.dto.opinion.OpinionResponse;
import com.discussion.ryu.entity.*;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.UserNotAuthorException;
import com.discussion.ryu.repository.DiscussionPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscussionPostService {

    private final DiscussionPostRepository discussionPostRepository;
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
        return DiscussionPostResponse.from(savedPost);
    }

    public Page<DiscussionPostResponse> getAllPosts(Pageable pageable) {
        return discussionPostRepository.findAllWithAuthor(pageable)
                .map(DiscussionPostResponse::from);
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

    public Page<DiscussionPostResponse> searchPosts(DiscussionSearchDto searchDto, Pageable pageable) {
        String keyword = searchDto.getKeyword();
        String authorName = searchDto.getAuthorName();
        DiscussionSearchDto.SearchType searchType = searchDto.getSearchType();

        // 1. keyword와 authorName 모두 있는 경우
        if (hasText(keyword) && hasText(authorName)) {
            return searchByKeywordAndAuthor(keyword, authorName, searchType, pageable);
        }

        // 2. keyword만 있는 경우
        if (hasText(keyword)) {
            return searchByKeywordOnly(keyword, searchType, pageable);
        }

        // 3. authorName만 있는 경우
        if (hasText(authorName)) {
            return discussionPostRepository.searchByAuthorName(authorName, pageable)
                    .map(DiscussionPostResponse::from);
        }

        // 4. 검색 조건이 없는 경우 전체 목록 반환
        return getAllPosts(pageable);
    }

    private Page<DiscussionPostResponse> searchByKeywordOnly(String keyword, 
                                                              DiscussionSearchDto.SearchType searchType, 
                                                              Pageable pageable) {
        Page<DiscussionPost> posts;
        
        if (searchType == DiscussionSearchDto.SearchType.TITLE) {
            posts = discussionPostRepository.searchByTitle(keyword, pageable);
        } else if (searchType == DiscussionSearchDto.SearchType.CONTENT) {
            posts = discussionPostRepository.searchByContent(keyword, pageable);
        } else {
            posts = discussionPostRepository.searchByKeyword(keyword, pageable);
        }

        return posts.map(DiscussionPostResponse::from);
    }

    private Page<DiscussionPostResponse> searchByKeywordAndAuthor(String keyword, 
                                                                   String authorName,
                                                                   DiscussionSearchDto.SearchType searchType, 
                                                                   Pageable pageable) {
        Page<DiscussionPost> posts;
        
        if (searchType == DiscussionSearchDto.SearchType.TITLE) {
            posts = discussionPostRepository.searchByTitleAndAuthor(keyword, authorName, pageable);
        } else if (searchType == DiscussionSearchDto.SearchType.CONTENT) {
            posts = discussionPostRepository.searchByContentAndAuthor(keyword, authorName, pageable);
        } else {
            posts = discussionPostRepository.searchByKeywordAndAuthor(keyword, authorName, pageable);
        }

        return posts.map(DiscussionPostResponse::from);
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
