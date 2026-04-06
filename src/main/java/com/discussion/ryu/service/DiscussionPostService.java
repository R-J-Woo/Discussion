package com.discussion.ryu.service;

import com.discussion.ryu.dto.discussion.*;
import com.discussion.ryu.dto.opinion.OpinionResponse;
import com.discussion.ryu.entity.*;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.UserNotAuthorException;
import com.discussion.ryu.repository.DiscussionPostRepository;
import com.discussion.ryu.repository.DiscussionPostSearchRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscussionPostService {

    private final DiscussionPostRepository discussionPostRepository;
    private final DiscussionPostSearchRepositoryImpl discussionPostSearchRepository;
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

    public Page<DiscussionPostResponse> getAllPosts(SortType sortType, Pageable pageable) {
        Pageable sortedPageable = createSortedPageable(sortType, pageable);
        return discussionPostRepository.findAllWithAuthor(sortedPageable)
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
        DiscussionSearchDto.SearchType searchType = searchDto.getSearchType();
        SortType sortType = searchDto.getSortType();

        return discussionPostSearchRepository
                .searchPosts(keyword, searchType, pageable, sortType)
                .map(DiscussionPostResponse::from);
    }

    private Page<DiscussionPostResponse> searchByKeywordOnly(String keyword, 
                                                              DiscussionSearchDto.SearchType searchType, 
                                                              Pageable pageable) {
        Page<DiscussionPost> result;
        
        if (searchType == DiscussionSearchDto.SearchType.TITLE) {
            result = discussionPostRepository.searchByTitle(keyword, pageable);
        } else if (searchType == DiscussionSearchDto.SearchType.CONTENT) {
            result = discussionPostRepository.searchByContent(keyword, pageable);
        } else {
            result = discussionPostRepository.searchByKeyword(keyword, pageable);
        }

        return result.map(DiscussionPostResponse::from);
    }

    private Page<DiscussionPostResponse> searchByKeywordAndAuthor(String keyword, 
                                                                   String authorName,
                                                                   DiscussionSearchDto.SearchType searchType, 
                                                                   Pageable pageable) {
        Page<DiscussionPost> result;
        
        if (searchType == DiscussionSearchDto.SearchType.TITLE) {
            result = discussionPostRepository.searchByTitleAndAuthor(keyword, authorName, pageable);
        } else if (searchType == DiscussionSearchDto.SearchType.CONTENT) {
            result = discussionPostRepository.searchByContentAndAuthor(keyword, authorName, pageable);
        } else {
            result = discussionPostRepository.searchByKeywordAndAuthor(keyword, authorName, pageable);
        }

        return result.map(DiscussionPostResponse::from);
    }

    private Pageable createSortedPageable(SortType sortType, Pageable pageable) {
        if (sortType == null) {
            sortType = SortType.LATEST;
        }

        Sort sort = createSort(sortType);
        
        return PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            sort
        );
    }

    private Sort createSort(SortType sortType) {
        if (sortType == null) {
            sortType = SortType.LATEST;
        }

        if (sortType == SortType.LATEST) {
            // 최신순: 작성일 내림차순
            return Sort.by(Sort.Direction.DESC, "createdAt");
        } else if (sortType == SortType.POPULAR) {
            // 인기순: 찬성+반대 합계 내림차순, 동점이면 최신순
            return Sort.by(Sort.Direction.DESC, "agreeCount")
                    .and(Sort.by(Sort.Direction.DESC, "disagreeCount"))
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else if (sortType == SortType.MOST_AGREED) {
            // 찬성 많은 순: 찬성수 내림차순, 동점이면 최신순
            return Sort.by(Sort.Direction.DESC, "agreeCount")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            // 반대 많은 순: 반대수 내림차순, 동점이면 최신순
            return Sort.by(Sort.Direction.DESC, "disagreeCount")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
