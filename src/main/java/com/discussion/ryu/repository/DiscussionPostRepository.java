package com.discussion.ryu.repository;

import com.discussion.ryu.dto.discussion.DiscussionSearchDto;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
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
public interface DiscussionPostRepository extends JpaRepository<DiscussionPost, Long> {

    @Query("SELECT d FROM DiscussionPost d JOIN FETCH d.author")
    Page<DiscussionPost> findAllWithAuthor(Pageable pageable);

    @Query("SELECT d FROM DiscussionPost d JOIN FETCH d.author WHERE d.author = :user")
    Page<DiscussionPost> findByAuthor(User user, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE DiscussionPost d SET d.agreeCount = d.agreeCount + 1 WHERE d.id = :id")
    void incrementAgreeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE DiscussionPost d SET d.agreeCount = GREATEST(0, d.agreeCount - 1) WHERE d.id = :id")
    void decrementAgreeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE DiscussionPost d SET d.disagreeCount = d.disagreeCount + 1 WHERE d.id = :id")
    void incrementDisagreeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE DiscussionPost d SET d.disagreeCount = GREATEST(0, d.disagreeCount - 1) WHERE d.id = :id")
    void decrementDisagreeCount(@Param("id") Long id);
}
