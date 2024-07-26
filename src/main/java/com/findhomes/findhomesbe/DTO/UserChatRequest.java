package com.findhomes.findhomesbe.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserChatRequest {
    @Schema(description = "사용자 입력입니다.")
    private String userInput;
}
