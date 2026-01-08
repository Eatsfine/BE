package com.eatsfine.eatsfine.domain.store.repository;

import com.eatsfine.eatsfine.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
