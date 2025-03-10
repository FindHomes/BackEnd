package com.findhomes.findhomesbe.domain.condition.dto;

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
    private List<String> result;
}
