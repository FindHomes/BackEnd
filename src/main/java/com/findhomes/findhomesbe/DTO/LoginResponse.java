package com.findhomes.findhomesbe.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private JwtToken result;
    @AllArgsConstructor
    public static class JwtToken{
        @Schema(description = "토큰 값 입니다.")
        String token;
        @Schema(description = "토큰 타입입니다.")
        String type;
        @Schema(description = "토큰 만료시간입니다.")
        String expires_in;

    }


}