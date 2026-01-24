package com.eatsfine.eatsfine.domain.user.repository;

import com.eatsfine.eatsfine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
