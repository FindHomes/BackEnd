package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.entity.User;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.repository.UserRepository;
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
