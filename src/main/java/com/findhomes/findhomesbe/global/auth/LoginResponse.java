package com.findhomes.findhomesbe.global.auth;

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
        private String token;
        private RefreshToken refreshToken;
    }
}