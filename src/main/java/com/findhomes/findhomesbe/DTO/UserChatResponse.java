package com.findhomes.findhomesbe.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserChatResponse {
    @Schema(description = "사용자에게 보여지는 챗봇 응답입니다.")
    public String chatResponse;

}
