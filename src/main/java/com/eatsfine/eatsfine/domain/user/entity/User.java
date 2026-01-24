package com.eatsfine.eatsfine.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
// 수정한 부분: access 레벨을 PROTECTED로 설정하여 Hibernate가 접근할 수 있게 합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
