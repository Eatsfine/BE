package com.eatsfine.eatsfine.domain.store.repository;

import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {

    @Query("""
    select s from Store s 
    left join fetch s.menus m
    where s.id = :id
    and (m.deletedAt IS NULL or m.id IS NULL)

""")
    Optional<Store> findByIdWithMenus(@Param("id") Long id);

    List<Store> findAllByOwner(User owner);

}