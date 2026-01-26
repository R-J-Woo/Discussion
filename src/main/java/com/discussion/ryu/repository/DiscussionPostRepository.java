package com.discussion.ryu.repository;

import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscussionPostRepository extends JpaRepository<DiscussionPost, Long> {

    @Query("SELECT DISTINCT d FROM DiscussionPost d JOIN FETCH d.author")
    Page<DiscussionPost> findAllWithAuthor(Pageable pageable);

    @Query("SELECT d FROM DiscussionPost d JOIN FETCH d.author WHERE d.author = :user")
    Page<DiscussionPost> findByAuthor(User user, Pageable pageable);
}
