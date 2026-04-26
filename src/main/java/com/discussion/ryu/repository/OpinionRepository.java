package com.discussion.ryu.repository;

import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.Opinion;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OpinionRepository extends JpaRepository<Opinion, Long> {

    @Query("SELECT o FROM Opinion o JOIN FETCH o.author WHERE o.discussionPost = :discussionPost")
    Page<Opinion> findByDiscussionPost(DiscussionPost discussionPost, Pageable pageable);

    // 비관적 락을 사용한 findById - 반응(like/dislike) 업데이트 시 사용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Opinion o WHERE o.id = :id")
    Optional<Opinion> findByIdWithLock(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Opinion o SET o.likeCount = o.likeCount + 1 WHERE o.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Opinion o SET o.likeCount = GREATEST(0, o.likeCount - 1) WHERE o.id = :id")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Opinion o SET o.dislikeCount = o.dislikeCount + 1 WHERE o.id = :id")
    void incrementDislikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Opinion o SET o.dislikeCount = GREATEST(0, o.dislikeCount - 1) WHERE o.id = :id")
    void decrementDislikeCount(@Param("id") Long id);
}
