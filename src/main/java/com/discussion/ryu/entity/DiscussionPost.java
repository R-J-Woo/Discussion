package com.discussion.ryu.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discussion_posts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE discussion_posts SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class DiscussionPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @Column(nullable = false)
    private Long agreeCount = 0L;

    @Column(nullable = false)
    private Long disagreeCount = 0L;

    @OneToMany(mappedBy = "discussionPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscussionVote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "discussionPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Opinion> opinions = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void incrementAgreeCount() {
        this.agreeCount++;
    }

    public void incrementDisagreeCount() {
        this.disagreeCount++;
    }

    public void decrementAgreeCount() {
        if (this.agreeCount > 0) {
            this.agreeCount--;
        }
    }

    public void decrementDisagreeCount() {
        if (this.disagreeCount > 0) {
            this.disagreeCount--;
        }
    }
}
