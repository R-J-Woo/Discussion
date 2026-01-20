package com.discussion.ryu.repository;

import com.discussion.ryu.entity.AuthProvider;
import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscussionPostRepository extends JpaRepository<DiscussionPost, Long> {
    List<DiscussionPost> findByAuthor(User user);
}
