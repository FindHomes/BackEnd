package com.findhomes.findhomesbe.userchat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class UserChatResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private ChatResponse result;

    @Data
    @AllArgsConstructor
    public static class ChatResponse {
        @Schema(description = "사용자에게 보여지는 챗봇 응답입니다.")
        public String chatResponse;
    }
}
