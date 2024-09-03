package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.UserChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserChatRepository extends JpaRepository<UserChat,Integer> {
    List<UserChat> findBySessionId(String token);
}
