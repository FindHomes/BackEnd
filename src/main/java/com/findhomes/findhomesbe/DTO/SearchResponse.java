package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.entity.House;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private SearchResult result;

    @Data
    public static class SearchResult {
        private List<House> houses;

        private Double xMin;
        private Double xMax;
        private Double yMin;
        private Double yMax;
    }

}
