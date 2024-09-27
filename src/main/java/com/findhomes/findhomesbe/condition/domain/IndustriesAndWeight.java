package com.findhomes.findhomesbe.condition.domain;

import com.findhomes.findhomesbe.entity.industry.Industry;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IndustriesAndWeight {
    private List<Industry> industries;
    private Integer weight;
    private Double maxRadius;
}
