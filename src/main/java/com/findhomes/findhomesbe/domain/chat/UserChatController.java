package com.findhomes.findhomesbe.domain.chat;

import com.findhomes.findhomesbe.domain.condition.dto.ManConRequest;
import com.findhomes.findhomesbe.domain.chat.gpt.ChatGPTConst;
import com.findhomes.findhomesbe.domain.condition.domain.FacilityCategory;
import com.findhomes.findhomesbe.domain.condition.domain.PublicData;
import com.findhomes.findhomesbe.domain.condition.domain.SessionKeys;
import com.findhomes.findhomesbe.domain.chat.gpt.ChatGPTServiceImpl;
import com.findhomes.findhomesbe.global.auth.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

import static com.findhomes.findhomesbe.domain.chat.gpt.ChatGPTConst.CHAT_TEMPERATURE;
import static com.findhomes.findhomesbe.domain.chat.gpt.CommandService.createChatCommand;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserChatController {
    private final SecurityService securityService;
    private final UserChatService userChatService;
    private final ChatGPTServiceImpl chatGPTServiceImpl;

    @PostMapping("/api/search/user-chat")
    @Operation(summary = "사용자 채팅", description = "사용자 입력을 받고, 챗봇의 응답을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "챗봇 응답 완료", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserChatResponse.class))})
    @ApiResponse(responseCode = "204", description = "챗봇 대화 종료", content = {@Content(mediaType = "application/json")})
    public ResponseEntity<UserChatResponse> userChat(@RequestBody UserChatRequest userChatRequest, HttpServletRequest httpRequest, @Parameter(hidden = true) @SessionAttribute(value = SessionKeys.MAN_CON_KEY, required = false) ManConRequest manConRequest) {
        HttpSession session = securityService.getSession(httpRequest);
        String chatSessionId = session.getId();

        // 이전 대화 내용을 가져오기
        List<UserChat> previousChats = userChatService.getUserChatsBySessionId(chatSessionId);
        StringBuilder conversation = userChatService.buildConversation(previousChats);

        // 사용자 입력 추가 및 대화 응답 조정
        String command = createChatCommand(userChatRequest.getUserInput(), FacilityCategory.getAllData() + PublicData.getAllData());
        conversation.append(command);

        // GPT에게 요청 보내기 (여기서 gptService를 사용하여 GPT 응답을 가져옵니다)
        String gptResponse = chatGPTServiceImpl.getGptOutput(conversation.toString(), ChatGPTConst.ROLE1, ChatGPTConst.ROLE2, ChatGPTConst.CHAT_CONTENT, CHAT_TEMPERATURE).replaceAll("[^가-힣,.!? ]", "").trim();
        System.out.println(gptResponse);

        // 사용자 입력과 GPT 응답 저장
        userChatService.saveUserChat(chatSessionId, userChatRequest.getUserInput(), gptResponse);

        // 응답 반환
        UserChatResponse response = new UserChatResponse(true, 200, "성공", new UserChatResponse.ChatResponse(gptResponse));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
