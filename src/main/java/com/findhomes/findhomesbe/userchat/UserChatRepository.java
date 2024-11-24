package com.findhomes.findhomesbe.userchat;

import com.findhomes.findhomesbe.entity.UserChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface UserChatRepository extends JpaRepository<UserChat,Integer> {
    List<UserChat> findBySessionId(String token);
}
