package com.discussion.ryu.repository;

import com.discussion.ryu.entity.Opinion;
import com.discussion.ryu.entity.OpinionReaction;
import com.discussion.ryu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OpinionReactionRepository extends JpaRepository<OpinionReaction, Long> {

    Optional<OpinionReaction> findByUserAndOpinion(User user, Opinion opinion);
    
    // 동시성 테스트를 위한 카운트 메서드
    long countByOpinion(Opinion opinion);
    
    long countByOpinionAndUser(Opinion opinion, User user);
}
