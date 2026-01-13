package com.discussion.ryu.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "providerId"})
        }
)
@Data
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
    private String username;

    @Column
    private String password;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @Column
    private String grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider; // LOCAL, GOOGLE, KAKAO

    @Column(nullable = false)
    private String providerId;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime created_at;

    @UpdateTimestamp
    private ZonedDateTime updated_at;

    @Column
    private ZonedDateTime deleted_at;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
