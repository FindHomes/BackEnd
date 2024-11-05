package com.findhomes.findhomesbe.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private Tokens result;
    @Data
    @AllArgsConstructor
    @Builder
    public static class Tokens {
        private JwtToken accessToken;
        private JwtToken refreshToken;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class JwtToken {
        @Schema(description = "토큰 값", example = "eyJhbGciOiJIUzI1NiJ9...")
        private String token;
        @Schema(description = "토큰 타입", example = "Bearer")
        private String type;
        @Schema(description = "토큰 만료 시간 (초 단위)", example = "3600")
        private long expiresIn;
    }


}