package com.discussion.ryu.repository;

import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.Opinion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpinionRepository extends JpaRepository<Opinion, Long> {
    @Query("SELECT o FROM Opinion o JOIN FETCH o.author WHERE o.discussionPost = :discussionPost")
    Page<Opinion> findByDiscussionPost(DiscussionPost discussionPost, Pageable pageable);
}
