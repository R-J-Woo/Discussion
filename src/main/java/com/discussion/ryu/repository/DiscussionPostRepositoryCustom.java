package com.discussion.ryu.repository;

import com.discussion.ryu.dto.discussion.DiscussionSearchDto;
import com.discussion.ryu.dto.discussion.SortType;
import com.discussion.ryu.entity.DiscussionPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DiscussionPostRepositoryCustom {
    Page<DiscussionPost> searchPosts(
            String keyword,
            DiscussionSearchDto.SearchType searchType,
            Pageable pageable,
            SortType sortType
    );
}
