package com.findhomes.findhomesbe.domain.user;

import com.findhomes.findhomesbe.global.exception.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    public User getUser(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if(user==null){
            throw new DataNotFoundException("유저가 존재하지 않습니다.");
        }
        return user;
    }
}
