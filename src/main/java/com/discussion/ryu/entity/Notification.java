package com.discussion.ryu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
@lombok.Getter
=======
>>>>>>> 2d2a951a0eac320e79e3756153b16bf503b05b3f
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opinion_id")
    private Opinion opinion;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    @Column(nullable = false)
    private boolean isSent = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
<<<<<<< HEAD

    public void setSent(boolean sent) {
        this.isSent = sent;
    }
=======
>>>>>>> 2d2a951a0eac320e79e3756153b16bf503b05b3f
}
