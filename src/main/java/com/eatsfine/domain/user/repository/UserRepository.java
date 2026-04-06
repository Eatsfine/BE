package com.eatsfine.domain.user.repository;

import com.eatsfine.domain.user.entity.User;
import com.eatsfine.domain.user.enums.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
    boolean existsByEmail(String email);
}
