package com.discussion.ryu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "opinion_reactions",
        uniqueConstraints = @UniqueConstraint (columnNames = {"user_id", "opinion_id"})
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpinionReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opinion_id", nullable = false)
    private Opinion opinion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType reactionType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public void changeReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }
}
