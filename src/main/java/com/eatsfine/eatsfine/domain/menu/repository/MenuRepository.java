package com.eatsfine.eatsfine.domain.menu.repository;

import com.eatsfine.eatsfine.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
