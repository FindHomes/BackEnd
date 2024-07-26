package com.findhomes.findhomesbe.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserChatResponse {
    @Schema(description = "사용자에게 보여지는 챗봇 응답입니다.")
    private String chatResponse;
}
