package com.eatsfine.eatsfine.domain.term.entity;

import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "term")
public class Term extends BaseEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
        @JoinColumn(name = "user_id", nullable = false, unique = true)
        private User user;

        @Builder.Default
        @Column(name = "tos_consent", nullable = false)
        private Boolean tosConsent = true;

        @Builder.Default
        @Column(name = "privacy_consent", nullable = false)
        private Boolean privacyConsent = true;


        @Column(name = "marketing_consent", nullable = false)
        private Boolean marketingConsent;

}
