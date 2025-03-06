package com.findhomes.findhomesbe.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserChatService {

    private final UserChatRepository userChatRepository;

    public List<UserChat> getUserChatsBySessionId(String sessionId) {
        return userChatRepository.findBySessionId(sessionId);
    }

    public void saveUserChat(String sessionId, String userInput, String gptResponse) {
        UserChat userChat = new UserChat();
        userChat.setSessionId(sessionId);
        userChat.setUserInput(userInput);
        userChat.setGptResponse(gptResponse);
        userChat.setCreatedAt(LocalDateTime.now());
        userChatRepository.save(userChat);
    }

    public StringBuilder buildConversation(List<UserChat> previousChats) {
        if (previousChats == null || previousChats.isEmpty()) {
            return new StringBuilder("[이전 대화 기록 없음]");
        }

        StringBuilder conversation = new StringBuilder("[이전 대화 기록]\n");
        for (UserChat chat : previousChats) {
            conversation.append("사용자: ").append(chat.getUserInput()).append("\n");
            if (chat.getGptResponse() != null) {
                conversation.append("챗봇: ").append(chat.getGptResponse()).append("\n");
            }
        }
        return conversation;
    }
}