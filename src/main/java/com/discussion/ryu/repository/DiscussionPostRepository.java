package com.discussion.ryu.repository;

import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    // 비관적 락을 사용한 findById - 투표/반응 업데이트 시 사용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DiscussionPost d WHERE d.id = :id")
    Optional<DiscussionPost> findByIdWithLock(@Param("id") Long id);

    // 검색 기능
    // keyword -> title & content
    @Query("SELECT DISTINCT d FROM DiscussionPost d JOIN FETCH d.author " +
            "WHERE (d.title LIKE %:keyword% OR d.content LIKE %:keyword%)")
    Page<DiscussionPost> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT d FROM DiscussionPost d JOIN FETCH d.author " +
            "WHERE d.title LIKE %:keyword%")
    Page<DiscussionPost> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT d FROM DiscussionPost d JOIN FETCH d.author " +
            "WHERE d.content LIKE %:keyword%")
    Page<DiscussionPost> searchByContent(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT d FROM DiscussionPost d JOIN FETCH d.author " +
            "WHERE d.author.name LIKE %:authorName%")
    Page<DiscussionPost> searchByAuthorName(@Param("authorName") String authorName, Pageable pageable);

    @Query("SELECT DISTINCT d FROM DiscussionPost d JOIN FETCH d.author " +
            "WHERE (d.title LIKE %:keyword% OR d.content LIKE %:keyword%) " +
            "AND d.author.name LIKE %:authorName%")
    Page<DiscussionPost> searchByKeywordAndAuthor(
            @Param("keyword") String keyword,
            @Param("authorName") String authorName,
            Pageable pageable
    );

    @Query("SELECT DISTINCT d FROM DiscussionPost d JOIN FETCH d.author " +
            "WHERE d.title LIKE %:keyword% AND d.author.name LIKE %:authorName%")
    Page<DiscussionPost> searchByTitleAndAuthor(
            @Param("keyword") String keyword,
            @Param("authorName") String authorName,
            Pageable pageable
    );

    @Query("SELECT DISTINCT d FROM DiscussionPost d JOIN FETCH d.author " +
            "WHERE d.content LIKE %:keyword% AND d.author.name LIKE %:authorName%")
    Page<DiscussionPost> searchByContentAndAuthor(
            @Param("keyword") String keyword,
            @Param("authorName") String authorName,
            Pageable pageable
    );
}
