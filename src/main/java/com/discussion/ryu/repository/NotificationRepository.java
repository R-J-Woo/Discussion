package com.discussion.ryu.repository;

import com.discussion.ryu.entity.Notification;
import com.discussion.ryu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 특정 사용자의 모든 알림 조회 (페이징)
     */
    Page<Notification> findByUser(User user, Pageable pageable);
    
    /**
     * 특정 사용자의 미발송 알림 조회
     */
    List<Notification> findByUserAndIsSentFalse(User user);
}
