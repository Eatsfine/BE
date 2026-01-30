package com.eatsfine.eatsfine.domain.menu.entity;

import com.eatsfine.eatsfine.domain.menu.enums.MenuCategory;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "menu")
@SQLDelete(sql = "UPDATE menu SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "price", precision = 10, scale = 0, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_category", nullable = false)
    private MenuCategory menuCategory;

    @Column(name = "image_key")
    private String imageKey;

    @Builder.Default
    @Column(name = "is_sold_out", nullable = false)
    private boolean isSoldOut = false;

    private LocalDateTime deletedAt;

    public void assignStore(Store store) {
        this.store = store;
    }

    // 품절 여부 변경
    public void updateSoldOut(boolean isSoldOut) {
        this.isSoldOut = isSoldOut;
    }

    // 메뉴 이미지 변경
    public void updateImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    // --- 메뉴 정보 수정 메서드 ---

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updatePrice(BigDecimal price) {
        this.price = price;
    }

    public void updateCategory(MenuCategory menuCategory) {
        this.menuCategory = menuCategory;
    }
}
