package com.eatsfine.eatsfine.global.auth;




import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.eatsfine.eatsfine.domain.user.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + email));

        String password = user.getPassword();
        if (password == null) {
            throw new UsernameNotFoundException("비밀번호 기반 로그인 대상이 아닙니다.");
        }

        return new User(user.getEmail(), password, List.of());
    }
}

