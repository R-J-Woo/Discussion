package com.discussion.ryu.repository;

import com.discussion.ryu.entity.FcmToken;
import com.discussion.ryu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    
    /**
     * 사용자의 모든 FCM 토큰 조회
     */
    List<FcmToken> findByUser(User user);
}
