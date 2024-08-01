package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.entity.House;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.util.List;

@Data
@NoArgsConstructor
public class SearchResponse {
    private List<House> houses;

    private Double xMin;
    private Double xMax;
    private Double yMin;
    private Double yMax;
}
