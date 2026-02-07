package com.eatsfine.eatsfine.domain.user.entity;

import com.eatsfine.eatsfine.domain.term.entity.Term;
import com.eatsfine.eatsfine.domain.user.enums.Role;
import com.eatsfine.eatsfine.domain.user.enums.SocialType;
import com.eatsfine.eatsfine.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter
// 수정한 부분: access 레벨을 PROTECTED로 설정하여 Hibernate가 접근할 수 있게 합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = true, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "social_id", unique = true)
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type")
    private SocialType socialType;

    @Column(nullable = true)
    private String profileImage;

    @Column(length = 500)
    private String refreshToken;

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updateToOwner() {
        this.role = Role.ROLE_OWNER;
    }

    public void updateRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }

    public void setTerm(Term term) {
        if (this.term != null && this.term != term) {
            this.term.setUser(null);
        }
        this.term = term;
        if (term != null) {
            term.setUser(this);
        }
    }

    public void linkSocial(SocialType socialType, String socialId) {
        this.socialType = socialType;
        this.socialId = socialId;
    }

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private Term term;

}