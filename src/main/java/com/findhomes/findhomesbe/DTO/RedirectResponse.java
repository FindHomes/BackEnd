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
public class RedirectResponse {
    private Boolean success;
    private Integer code;
    private String message;
    @Schema(description = "리다이렉트 주소입니다. 클라이언트에서 해당 주소에 대해 GET으로 리다이렉트가 진행됩니다.")
    private String result;



}