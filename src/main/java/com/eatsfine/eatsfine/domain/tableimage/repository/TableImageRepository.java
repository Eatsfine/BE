package com.eatsfine.eatsfine.domain.tableimage.repository;

import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.tableimage.entity.TableImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TableImageRepository extends JpaRepository<TableImage, Long> {

    @Query("""
    select coalesce(max(ti.imageOrder), 0)
    from TableImage ti
    where ti.store.id = :storeId
""")
    int findMaxOrderByStoreId(Long storeId);

    List<TableImage> findAllByStoreOrderByImageOrder(Store store);

    Optional<TableImage> findByIdAndStore(Long id, Store store);
}
