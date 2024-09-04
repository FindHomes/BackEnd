package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.entity.House;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.util.List;
import java.util.OptionalDouble;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private SearchResult result;

    public SearchResponse(List<House> houses, Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.result = SearchResult.of(houses);
    }

    @Data
    public static class SearchResult {
        private List<House> houses;

        private Double xMin;
        private Double xMax;
        private Double yMin;
        private Double yMax;

        // 팩토리 메서드 house 리스트에 대해서 최소 최대 위도와 경도를 계산해서 넣어서 SearchResult 객체를 생성해서 반환해줌.
        public static SearchResult of(List<House> houses) {
            SearchResult result = new SearchResult();
            result.setHouses(houses);

            // xMin, xMax 계산
            OptionalDouble xMin = houses.stream()
                    .mapToDouble(House::getX)
                    .min();
            OptionalDouble xMax = houses.stream()
                    .mapToDouble(House::getX)
                    .max();

            // yMin, yMax 계산
            OptionalDouble yMin = houses.stream()
                    .mapToDouble(House::getY)
                    .min();
            OptionalDouble yMax = houses.stream()
                    .mapToDouble(House::getY)
                    .max();

            // OptionalDouble 값이 존재할 경우에만 설정
            xMin.ifPresent(result::setXMin);
            xMax.ifPresent(result::setXMax);
            yMin.ifPresent(result::setYMin);
            yMax.ifPresent(result::setYMax);

            return result;
        }
    }
}
