package com.findhomes.findhomesbe.login;

import com.findhomes.findhomesbe.entity.User;
import com.findhomes.findhomesbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findByKakaoId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // 기본 UserDetails 사용
        return new org.springframework.security.core.userdetails.User(
                user.getKakaoId(),
                "", // 비밀번호가 필요 없는 경우 빈 문자열로 설정
                Collections.emptyList() // 권한이 필요한 경우 여기에 추가
        );
    }
}
