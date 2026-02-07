package com.eatsfine.eatsfine.domain.term.entity;

import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

        @Setter
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false, unique = true)
        private User user;

        @Column(name = "tos_consent", nullable = false)
        private Boolean tosConsent;

        @Column(name = "privacy_consent", nullable = false)
        private Boolean privacyConsent;

        @Column(name = "marketing_consent", nullable = false)
        private Boolean marketingConsent;
}
