package com.discussion.ryu.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "discussion_votes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "post_id"})  // 한 사용자당 한 토론글에 1표만
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private DiscussionPost discussionPost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType voteType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void changeVoteType(VoteType voteType) {
        this.voteType = voteType;
    }
}
