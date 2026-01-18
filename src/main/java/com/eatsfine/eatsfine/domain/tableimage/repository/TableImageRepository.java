package com.eatsfine.eatsfine.domain.tableimage.repository;

import com.eatsfine.eatsfine.domain.tableimage.entity.TableImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TableImageRepository extends JpaRepository<TableImage, Long> {

    @Query("""
    select coalesce(max(ti.imageOrder), 0)
    from TableImage ti
    where ti.store.id = :storeId
""")
    int findMaxOrderByStoreId(Long storeId);
}
