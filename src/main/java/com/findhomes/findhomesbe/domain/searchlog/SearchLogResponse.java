package com.findhomes.findhomesbe.domain.searchlog;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchLogResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private List<SearchLogDto> result;
}
