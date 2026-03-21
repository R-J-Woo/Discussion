package com.discussion.ryu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "opinions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE opinions SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Opinion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private DiscussionPost discussionPost;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpinionStance stance;

    @Builder.Default
    @Column(nullable = false)
    private Long likeCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long dislikeCount = 0L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    public void updateOpinion(String content, OpinionStance stance) {
        this.content = content;
        this.stance = stance;
    }

    // 좋아요 증가
    public void incrementLikeCount() {
        this.likeCount += 1;
    }

    // 좋아요 감소
    public void decrementLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    // 싫어요 증가
    public void incrementDislikeCount() {
        this.dislikeCount += 1;
    }

    // 싫어요 감소
    public void decrementDislikeCount() {
        this.dislikeCount = Math.max(0, this.dislikeCount - 1);
    }
}
