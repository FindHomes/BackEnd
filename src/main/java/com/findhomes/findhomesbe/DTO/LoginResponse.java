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
    @Data
    public static class JwtToken{
        @Schema(description = "토큰 값 입니다.", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1ZTllODM3Ny03YWY5LTQwZDYtOTI1NC0xNTMzZjllYTMxOTciLCJpYXQiOjE3MjQ5MjM5NTksImV4cCI6MTcyNDkyNzU1OX0.54RvBsPMqaeiSDfXr1sjb3LyVop1MhsjXz2Dyb8pHN8")
        String token;
        @Schema(description = "토큰 타입입니다.", example = "Bearer")
        String type;
        @Schema(description = "토큰 만료시간입니다.", example = "3600000")
        String expires_in;

    }


}