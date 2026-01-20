package com.discussion.ryu.repository;

import com.discussion.ryu.entity.DiscussionPost;
import com.discussion.ryu.entity.DiscussionVote;
import com.discussion.ryu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscussionVoteRepository extends JpaRepository<DiscussionVote, Long> {

    Optional<DiscussionVote> findByUserAndDiscussionPost(User user, DiscussionPost discussionPost);
    boolean existsByUserAndDiscussionPost(User user, DiscussionPost discussionPost);
    void deleteByDiscussionPost(DiscussionPost discussionPost);
}
