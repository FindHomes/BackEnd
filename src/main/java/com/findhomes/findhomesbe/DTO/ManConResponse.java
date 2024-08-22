package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.entity.House;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
public class ManConResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private JSESSIONID result;

    @Data
    @AllArgsConstructor
    public static class JSESSIONID {
        @Schema(description = "세션 값입니다.")
        public String sessionId;
    }
}
