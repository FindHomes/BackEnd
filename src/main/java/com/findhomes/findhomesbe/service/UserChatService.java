package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.entity.UserChat;
import com.findhomes.findhomesbe.repository.UserChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserChatService {

    private final UserChatRepository userChatRepository;

    public List<UserChat> getUserChatsByToken(String token) {
        return userChatRepository.findByToken(token);
    }

    public void saveUserChat(String token, String userInput, String gptResponse) {
        UserChat userChat = new UserChat();
        userChat.setToken(token);
        userChat.setUserInput(userInput);
        userChat.setGptResponse(gptResponse);
        userChat.setCreatedAt(LocalDateTime.now());
        userChatRepository.save(userChat);
    }
}