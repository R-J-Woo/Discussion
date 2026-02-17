package com.discussion.ryu.repository;

import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface DiscussionPostRepository extends JpaRepository<DiscussionPost, Long> {

    @Query("SELECT DISTINCT d FROM DiscussionPost d JOIN FETCH d.author")
    Page<DiscussionPost> findAllWithAuthor(Pageable pageable);

    @Query("SELECT d FROM DiscussionPost d JOIN FETCH d.author WHERE d.author = :user")
    Page<DiscussionPost> findByAuthor(User user, Pageable pageable);

    @Modifying
    @Query("UPDATE DiscussionPost p SET p.agreeCount = p.agreeCount + 1 WHERE p.id = :id")
    void incrementAgreeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE DiscussionPost p SET p.agreeCount = p.agreeCount - 1 WHERE p.id = :id AND p.agreeCount > 0")
    void decrementAgreeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE DiscussionPost p SET p.disagreeCount = p.disagreeCount + 1 WHERE p.id = :id")
    void incrementDisagreeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE DiscussionPost p SET p.disagreeCount = p.disagreeCount - 1 WHERE p.id = :id AND p.disagreeCount > 0")
    void decrementDisagreeCount(@Param("id") Long id);
}
