package com.findhomes.findhomesbe.searchlog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchLogDto {
    private Integer searchLogId;
    private String date;
    private String keyword;
    private String regionInfo;
    private String typeInfo;
    private String priceInfo;
}
