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
        private String token;
        private String refreshToken;
    }
}