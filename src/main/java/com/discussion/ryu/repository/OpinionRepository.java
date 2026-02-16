package com.discussion.ryu.repository;

import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.Opinion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpinionRepository extends JpaRepository<Opinion, Long> {

    @Query("SELECT o FROM Opinion o JOIN FETCH o.author WHERE o.discussionPost = :discussionPost")
    Page<Opinion> findByDiscussionPost(DiscussionPost discussionPost, Pageable pageable);

    @Modifying
    @Query("UPDATE Opinion o SET o.likeCount = o.likeCount + 1 WHERE o.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Opinion o SET o.likeCount = o.likeCount - 1 WHERE o.id = :id AND o.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Opinion o SET o.dislikeCount = o.dislikeCount + 1 WHERE o.id = :id")
    void incrementDislikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Opinion o SET o.dislikeCount = o.dislikeCount - 1 WHERE o.id = :id AND o.dislikeCount > 0")
    void decrementDislikeCount(@Param("id") Long id);
}
