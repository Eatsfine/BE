package com.eatsfine.eatsfine.domain.menu.repository;

import com.eatsfine.eatsfine.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByImageKey(String imageKey);
}
