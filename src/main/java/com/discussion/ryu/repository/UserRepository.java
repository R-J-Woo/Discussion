package com.discussion.ryu.repository;

import com.discussion.ryu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    boolean existsByNameAndUserIdNot(String name, Long userId);
    boolean existsByEmailAndUserIdNot(String email, Long userId);
}
